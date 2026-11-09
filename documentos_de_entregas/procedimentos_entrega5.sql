USE jerseyhub;

DROP PROCEDURE IF EXISTS sp_reajustar_precos_equipe;

DELIMITER $$

CREATE PROCEDURE sp_reajustar_precos_equipe(
    IN p_id_equipe INT,
    IN p_percentual DECIMAL(5,2)
)
BEGIN
    IF p_percentual IS NULL OR p_percentual = 0 OR p_percentual <= -100 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Percentual invalido: informe um valor diferente de zero e maior que -100.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM Equipe WHERE id_equipe = p_id_equipe) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Equipe nao encontrada.';
    END IF;

    UPDATE Camisa
    SET preco = ROUND(preco * (1 + p_percentual / 100), 2)
    WHERE fk_id_equipe = p_id_equipe;

    SELECT ROW_COUNT() AS Camisas_Reajustadas;
END$$

/*
Justificativa: Procedimento de atualizacao de dados que aplica um reajuste
percentual, positivo ou negativo, nos precos de todas as camisas de uma equipe.
Atende situacoes comuns da loja, como a virada de colecao ou uma promocao de um
time, sem editar camisa por camisa. Valida o percentual e a equipe antes de
alterar os dados, e cada mudanca fica registrada em Log_Camisa pelo trigger
trg_log_alteracao_camisa. Pedidos ja feitos nao mudam, pois guardam o proprio
valor em subtotal_item e valor_total.
*/

DELIMITER ;

-- Exemplos de uso:
-- CALL sp_reajustar_precos_equipe(1, 10.00);   -- aumento de 10%
-- CALL sp_reajustar_precos_equipe(1, -15.00);  -- desconto de 15%
-- SELECT * FROM Log_Camisa ORDER BY id_log DESC;

DROP PROCEDURE IF EXISTS sp_distribuir_remessa;

DELIMITER $$

CREATE PROCEDURE sp_distribuir_remessa(
    IN p_pecas INT,
    IN p_estoque_minimo INT,
    OUT p_pecas_restantes INT
)
BEGIN
    DECLARE v_fim BOOLEAN DEFAULT FALSE;
    DECLARE v_ordem INT DEFAULT 0;
    DECLARE v_id_camisa INT;
    DECLARE v_estoque INT;
    DECLARE v_total_vendido INT;
    DECLARE v_necessidade INT;
    DECLARE v_restante INT;

    DECLARE cur_camisas CURSOR FOR
        SELECT id_camisa,
               quantidade_estoque,
               fn_total_vendido_camisa(id_camisa) AS total_vendido
        FROM Camisa
        WHERE quantidade_estoque < p_estoque_minimo
        ORDER BY total_vendido DESC, quantidade_estoque ASC, id_camisa ASC;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_fim = TRUE;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_pecas IS NULL OR p_pecas <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Remessa invalida: informe uma quantidade de pecas maior que zero.';
    END IF;

    IF p_estoque_minimo IS NULL OR p_estoque_minimo <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Estoque minimo invalido: informe um valor maior que zero.';
    END IF;

    DROP TEMPORARY TABLE IF EXISTS tmp_resultado_remessa;
    CREATE TEMPORARY TABLE tmp_resultado_remessa (
        ordem INT PRIMARY KEY,
        id_camisa INT NOT NULL,
        total_vendido INT NOT NULL,
        estoque_antes INT NOT NULL,
        necessidade INT NOT NULL,
        situacao VARCHAR(30) NOT NULL,
        saldo_remessa INT NOT NULL
    );

    SET v_restante = p_pecas;

    START TRANSACTION;

    OPEN cur_camisas;

    percorrer: LOOP
        FETCH cur_camisas INTO v_id_camisa, v_estoque, v_total_vendido;
        IF v_fim THEN
            LEAVE percorrer;
        END IF;

        SET v_ordem = v_ordem + 1;
        SET v_necessidade = p_estoque_minimo - v_estoque;

        IF v_necessidade <= v_restante THEN
            UPDATE Camisa
            SET quantidade_estoque = quantidade_estoque + v_necessidade
            WHERE id_camisa = v_id_camisa;

            SET v_restante = v_restante - v_necessidade;

            INSERT INTO tmp_resultado_remessa
            VALUES (v_ordem, v_id_camisa, v_total_vendido, v_estoque, v_necessidade, 'Reposta', v_restante);
        ELSE
            INSERT INTO tmp_resultado_remessa
            VALUES (v_ordem, v_id_camisa, v_total_vendido, v_estoque, v_necessidade, 'Ignorada (saldo insuficiente)', v_restante);
        END IF;
    END LOOP;

    CLOSE cur_camisas;

    COMMIT;

    SET p_pecas_restantes = v_restante;

    SELECT r.ordem AS Ordem,
           COALESCE(e.nome, 'Sem Equipe') AS Equipe,
           c.modelo AS Modelo,
           c.versao AS Versao,
           c.tamanho AS Tamanho,
           r.total_vendido AS Total_Vendido,
           r.estoque_antes AS Estoque_Antes,
           r.necessidade AS Necessidade,
           r.situacao AS Situacao,
           r.saldo_remessa AS Saldo_Remessa
    FROM tmp_resultado_remessa r
    JOIN Camisa c ON c.id_camisa = r.id_camisa
    LEFT JOIN Equipe e ON e.id_equipe = c.fk_id_equipe
    ORDER BY r.ordem;
END$$

/*
Justificativa: Procedimento que distribui uma remessa limitada de pecas entre
as camisas com estoque abaixo do minimo. As camisas sao percorridas por um
CURSOR em ordem de prioridade: mais vendidas primeiro (fn_total_vendido_camisa),
depois menor estoque e, por fim, menor id. Cada camisa recebe a reposicao
completa ate o estoque minimo ou nada, pois o fornecedor envia lotes fechados
por modelo; se o saldo da remessa nao cobre a necessidade, a camisa e pulada e
a proxima e avaliada.

Por que precisa de cursor: a decisao de cada camisa depende do saldo que sobrou
das decisoes anteriores, inclusive das camisas puladas. Exemplo com remessa de
10 pecas e necessidades 6, 5 e 3: a primeira recebe 6 (sobram 4), a segunda
precisa de 5 e e pulada, e a terceira recebe 3 (sobra 1). Um UPDATE avalia as
linhas sem guardar esse saldo, e uma soma acumulada com funcao de janela
descontaria tambem as 5 pecas da camisa pulada, negando a reposicao da terceira.

As alteracoes rodam em uma transacao: se qualquer passo falhar, nenhuma camisa
e reposta. Cada reposicao fica registrada em Log_Camisa pelo trigger
trg_log_alteracao_camisa, e o procedimento devolve um relatorio de cada camisa
avaliada e o saldo final da remessa no parametro OUT.
*/

DELIMITER ;

-- Exemplos de uso:
-- CALL sp_distribuir_remessa(30, 20, @restante);  -- remessa de 30 pecas, estoque minimo 20
-- SELECT @restante AS Pecas_Restantes;
-- SELECT * FROM Log_Camisa WHERE campo = 'quantidade_estoque' ORDER BY id_log DESC;
