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

DESCRIBE equipe;

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

DESCRIBE clube;

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

    CONSTRAINT chk_pedido_valor
        CHECK (valor_total >= 0),

    CONSTRAINT fk_pedido_cliente
        FOREIGN KEY (fk_id_cliente)
        REFERENCES Cliente(id_cliente)
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

SHOW TABLES;