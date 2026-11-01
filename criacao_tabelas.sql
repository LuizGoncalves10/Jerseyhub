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