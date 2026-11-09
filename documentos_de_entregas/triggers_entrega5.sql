USE jerseyhub;

CREATE TABLE IF NOT EXISTS Log_Camisa (
    id_log INT AUTO_INCREMENT PRIMARY KEY,
    id_camisa INT NOT NULL,
    campo VARCHAR(30) NOT NULL,
    valor_antigo DECIMAL(10,2) NOT NULL,
    valor_novo DECIMAL(10,2) NOT NULL,
    usuario VARCHAR(100) NOT NULL,
    data_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_log_camisa_campo
        CHECK (campo IN ('preco', 'quantidade_estoque'))
);

DROP TRIGGER IF EXISTS trg_log_alteracao_camisa;

DELIMITER $$

CREATE TRIGGER trg_log_alteracao_camisa
AFTER UPDATE ON Camisa
FOR EACH ROW
BEGIN
    IF NEW.preco <> OLD.preco THEN
        INSERT INTO Log_Camisa (id_camisa, campo, valor_antigo, valor_novo, usuario)
        VALUES (NEW.id_camisa, 'preco', OLD.preco, NEW.preco, USER());
    END IF;

    IF NEW.quantidade_estoque <> OLD.quantidade_estoque THEN
        INSERT INTO Log_Camisa (id_camisa, campo, valor_antigo, valor_novo, usuario)
        VALUES (NEW.id_camisa, 'quantidade_estoque', OLD.quantidade_estoque, NEW.quantidade_estoque, USER());
    END IF;
END$$

/*
Justificativa: Trigger que atualiza a tabela de logs Log_Camisa sempre que o
preco ou o estoque de uma camisa muda, guardando o valor antigo, o valor novo,
o usuario do banco e a data e hora da alteracao. Preco e estoque afetam
diretamente o faturamento da loja, entao esse historico permite auditar
reajustes e investigar divergencias de estoque, seja qual for a origem da
alteracao. A coluna id_camisa nao tem chave estrangeira de proposito, para que
o historico continue existindo depois que uma camisa for excluida.
*/

DELIMITER ;

-- Exemplos de uso:
-- UPDATE Camisa SET preco = 399.90 WHERE id_camisa = 1;
-- SELECT * FROM Log_Camisa ORDER BY id_log DESC;

DROP TRIGGER IF EXISTS trg_baixa_estoque_item;
DROP TRIGGER IF EXISTS trg_devolve_estoque_item;
DROP TRIGGER IF EXISTS trg_devolve_estoque_pedido;

DELIMITER $$

CREATE TRIGGER trg_baixa_estoque_item
BEFORE INSERT ON Item_Pedido
FOR EACH ROW
BEGIN
    DECLARE v_estoque INT;
    DECLARE v_mensagem VARCHAR(255);

    SELECT quantidade_estoque INTO v_estoque
    FROM Camisa
    WHERE id_camisa = NEW.fk_id_camisa
    FOR UPDATE;

    IF v_estoque IS NOT NULL THEN
        IF v_estoque < NEW.quantidade THEN
            SET v_mensagem = CONCAT('Estoque insuficiente para a camisa ', NEW.fk_id_camisa,
                                    ': disponivel ', v_estoque, ', solicitado ', NEW.quantidade, '.');
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = v_mensagem;
        END IF;

        UPDATE Camisa
        SET quantidade_estoque = quantidade_estoque - NEW.quantidade
        WHERE id_camisa = NEW.fk_id_camisa;
    END IF;
END$$

/*
Justificativa: Trigger que da baixa no estoque da camisa sempre que um item de
pedido e registrado, e recusa a venda quando nao ha pecas suficientes. Antes
dele, o sistema vendia sem nunca reduzir o estoque, entao a quantidade
cadastrada deixava de refletir as pecas realmente disponiveis. A regra fica no
banco, valendo para qualquer origem da venda, e o FOR UPDATE trava a linha da
camisa para que duas vendas simultaneas nao usem a mesma peca. Quando a camisa
nao existe, o trigger nao faz nada e a chave estrangeira recusa o item.
*/

CREATE TRIGGER trg_devolve_estoque_item
AFTER DELETE ON Item_Pedido
FOR EACH ROW
BEGIN
    UPDATE Camisa
    SET quantidade_estoque = quantidade_estoque + OLD.quantidade
    WHERE id_camisa = OLD.fk_id_camisa;
END$$

/*
Justificativa: Trigger complementar que devolve ao estoque as pecas de um item
de pedido removido. E necessario porque a edicao de pedidos no sistema apaga os
itens antigos e insere os novos: sem a devolucao, editar um pedido baixaria o
estoque duas vezes.
*/

CREATE TRIGGER trg_devolve_estoque_pedido
BEFORE DELETE ON Pedido
FOR EACH ROW
BEGIN
    UPDATE Camisa c
    JOIN (
        SELECT fk_id_camisa, SUM(quantidade) AS quantidade
        FROM Item_Pedido
        WHERE fk_id_pedido = OLD.id_pedido
        GROUP BY fk_id_camisa
    ) itens ON itens.fk_id_camisa = c.id_camisa
    SET c.quantidade_estoque = c.quantidade_estoque + itens.quantidade;
END$$

/*
Justificativa: Trigger complementar que devolve ao estoque todas as pecas de um
pedido excluido (cancelado). E necessario porque, no MySQL, os itens apagados
pelo ON DELETE CASCADE da chave estrangeira nao disparam os triggers de
Item_Pedido, entao trg_devolve_estoque_item nao seria executado nesse caso.

Observacoes: estes triggers devem ser criados depois de carregar
insercao_dados.sql, pois o estoque do seed ja desconta as vendas cadastradas.
Um UPDATE direto na quantidade ou na camisa de um Item_Pedido nao ajusta o
estoque; o sistema nunca faz isso, pois edita pedidos apagando e reinserindo
os itens.
*/

DELIMITER ;

-- Exemplos de uso:
-- SELECT quantidade_estoque FROM Camisa WHERE id_camisa = 1;
-- INSERT INTO Item_Pedido (quantidade, subtotal_item, fk_id_pedido, fk_id_camisa) VALUES (2, 699.80, 1, 1);
-- SELECT quantidade_estoque FROM Camisa WHERE id_camisa = 1;  -- 2 pecas a menos
-- DELETE FROM Pedido WHERE id_pedido = 1;                      -- pecas devolvidas
