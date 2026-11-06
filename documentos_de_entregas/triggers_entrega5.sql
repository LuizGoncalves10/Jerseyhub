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
