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
