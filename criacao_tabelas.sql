DROP DATABASE IF EXISTS jerseyhub;

CREATE DATABASE jerseyhub;

USE jerseyhub;

CREATE TABLE Equipe (
    id_equipe INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    ano_fundacao INT NOT NULL,
    equipe_tipo INT NOT NULL DEFAULT 1,

    CONSTRAINT chk_equipe_ano
        CHECK (ano_fundacao BETWEEN 1850 AND 2100),

    CONSTRAINT chk_equipe_tipo
        CHECK (equipe_tipo IN (1, 2))
);

CREATE TABLE Clube (
    fk_id_equipe INT PRIMARY KEY,
    estadio VARCHAR(100) NOT NULL,
    liga VARCHAR(100) NOT NULL,

    CONSTRAINT fk_clube_equipe
        FOREIGN KEY (fk_id_equipe)
        REFERENCES Equipe(id_equipe)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE Selecao (
    fk_id_equipe INT PRIMARY KEY,
    pais VARCHAR(100) NOT NULL UNIQUE,
    confederacao VARCHAR(100) NOT NULL,

    CONSTRAINT fk_selecao_equipe
        FOREIGN KEY (fk_id_equipe)
        REFERENCES Equipe(id_equipe)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE Camisa (
    id_camisa INT AUTO_INCREMENT PRIMARY KEY,
    modelo VARCHAR(100) NOT NULL,
    versao VARCHAR(50) NOT NULL,
    tamanho VARCHAR(10) NOT NULL,
    preco DECIMAL(10,2) NOT NULL,
    ano INT NOT NULL,
    quantidade_estoque INT NOT NULL DEFAULT 0,
    fk_id_equipe INT,

    CONSTRAINT chk_camisa_versao
        CHECK (versao IN ('Torcedor', 'Jogador')),

    CONSTRAINT chk_camisa_tamanho
        CHECK (tamanho IN ('P', 'M', 'G', 'GG')),

    CONSTRAINT chk_camisa_preco
        CHECK (preco > 0),

    CONSTRAINT chk_camisa_ano
        CHECK (ano BETWEEN 1900 AND 2100),

    CONSTRAINT chk_camisa_estoque
        CHECK (quantidade_estoque >= 0),

    CONSTRAINT fk_camisa_equipe
        FOREIGN KEY (fk_id_equipe)
        REFERENCES Equipe(id_equipe)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

CREATE TABLE Camisa_Cor (
    fk_id_camisa INT NOT NULL,
    cor VARCHAR(50) NOT NULL,

    PRIMARY KEY (fk_id_camisa, cor),

    CONSTRAINT fk_camisa_cor
        FOREIGN KEY (fk_id_camisa)
        REFERENCES Camisa(id_camisa)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE Cliente (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf CHAR(11) NOT NULL UNIQUE,
    rua VARCHAR(100) NOT NULL,
    bairro VARCHAR(100) NOT NULL,
    numero INT NOT NULL,
    cep CHAR(8) NOT NULL,

    CONSTRAINT chk_cliente_numero
        CHECK (numero > 0)
);

CREATE TABLE Funcionario (
    id_funcionario INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cargo VARCHAR(100) NOT NULL
);

CREATE TABLE Supervisao (
    fk_id_supervisor INT NOT NULL,
    fk_id_supervisionado INT NOT NULL,

    PRIMARY KEY (fk_id_supervisor, fk_id_supervisionado),

    CONSTRAINT fk_supervisao_supervisor
        FOREIGN KEY (fk_id_supervisor)
        REFERENCES Funcionario(id_funcionario)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_supervisao_supervisionado
        FOREIGN KEY (fk_id_supervisionado)
        REFERENCES Funcionario(id_funcionario)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE Pedido (
    id_pedido INT AUTO_INCREMENT PRIMARY KEY,
    valor_total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    data_compra DATE NOT NULL,
    fk_id_cliente INT NOT NULL,
    fk_id_funcionario INT NOT NULL,

    CONSTRAINT chk_pedido_valor
        CHECK (valor_total >= 0),

    CONSTRAINT fk_pedido_cliente
        FOREIGN KEY (fk_id_cliente)
        REFERENCES Cliente(id_cliente)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_pedido_funcionario
        FOREIGN KEY (fk_id_funcionario)
        REFERENCES Funcionario(id_funcionario)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE TABLE Item_Pedido (
    id_item_pedido INT AUTO_INCREMENT PRIMARY KEY,
    quantidade INT NOT NULL DEFAULT 1,
    subtotal_item DECIMAL(10,2) NOT NULL,
    fk_id_pedido INT NOT NULL,
    fk_id_camisa INT NOT NULL,

    CONSTRAINT chk_item_quantidade
        CHECK (quantidade > 0),

    CONSTRAINT chk_item_subtotal
        CHECK (subtotal_item > 0),

    CONSTRAINT fk_item_pedido
        FOREIGN KEY (fk_id_pedido)
        REFERENCES Pedido(id_pedido)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_item_camisa
        FOREIGN KEY (fk_id_camisa)
        REFERENCES Camisa(id_camisa)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE TABLE Personalizacao (
    id_personalizacao INT AUTO_INCREMENT PRIMARY KEY,
    nome_costas VARCHAR(100) NOT NULL,
    numero_costas INT NOT NULL,
    fk_id_item_pedido INT NOT NULL UNIQUE,

    CONSTRAINT chk_personalizacao_numero
        CHECK (numero_costas BETWEEN 0 AND 99),

    CONSTRAINT fk_personalizacao_item
        FOREIGN KEY (fk_id_item_pedido)
        REFERENCES Item_Pedido(id_item_pedido)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE INDEX idx_cliente_nome ON Cliente(nome);

CREATE INDEX idx_funcionario_nome ON Funcionario(nome);

CREATE VIEW vw_faturamento_por_equipe AS
SELECT 
    e.nome AS Equipe,
    CASE 
        WHEN e.equipe_tipo = 1 THEN 'Clube'
        ELSE 'Seleção' 
    END AS Categoria,
    SUM(ip.quantidade) AS Total_Camisas_Vendidas,
    SUM(ip.subtotal_item) AS Faturamento_Total
FROM Equipe e
JOIN Camisa c ON e.id_equipe = c.fk_id_equipe
JOIN Item_Pedido ip ON c.id_camisa = ip.fk_id_camisa
JOIN Pedido p ON ip.fk_id_pedido = p.id_pedido
GROUP BY e.id_equipe, e.nome, e.equipe_tipo;

CREATE VIEW vw_desempenho_vendedores AS
SELECT 
    f.nome AS Vendedor,
    f.cargo AS Cargo,
    COUNT(DISTINCT p.id_pedido) AS Total_Pedidos,
    COUNT(DISTINCT p.fk_id_cliente) AS Clientes_Unicos_Atendidos,
    SUM(ip.subtotal_item) AS Valor_Total_Vendido
FROM Funcionario f
JOIN Pedido p ON f.id_funcionario = p.fk_id_funcionario
JOIN Item_Pedido ip ON p.id_pedido = ip.fk_id_pedido
JOIN Cliente cl ON p.fk_id_cliente = cl.id_cliente
GROUP BY f.id_funcionario, f.nome, f.cargo;

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

DROP FUNCTION IF EXISTS fn_classificar_cliente;
DROP FUNCTION IF EXISTS fn_total_vendido_camisa;

DELIMITER $$

CREATE FUNCTION fn_classificar_cliente(p_id_cliente INT)
RETURNS VARCHAR(20)
READS SQL DATA
BEGIN
    DECLARE v_total_gasto DECIMAL(12,2);

    IF NOT EXISTS (SELECT 1 FROM Cliente WHERE id_cliente = p_id_cliente) THEN
        RETURN NULL;
    END IF;

    SELECT SUM(valor_total) INTO v_total_gasto
    FROM Pedido
    WHERE fk_id_cliente = p_id_cliente;

    IF v_total_gasto IS NULL THEN
        RETURN 'Sem compras';
    ELSEIF v_total_gasto >= 900 THEN
        RETURN 'Ouro';
    ELSEIF v_total_gasto >= 500 THEN
        RETURN 'Prata';
    ELSE
        RETURN 'Bronze';
    END IF;
END$$

CREATE FUNCTION fn_total_vendido_camisa(p_id_camisa INT)
RETURNS INT
READS SQL DATA
BEGIN
    DECLARE v_total_vendido INT;

    SELECT COALESCE(SUM(quantidade), 0) INTO v_total_vendido
    FROM Item_Pedido
    WHERE fk_id_camisa = p_id_camisa;

    RETURN v_total_vendido;
END$$

DELIMITER ;

DROP PROCEDURE IF EXISTS sp_reajustar_precos_equipe;
DROP PROCEDURE IF EXISTS sp_distribuir_remessa;

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

DELIMITER ;