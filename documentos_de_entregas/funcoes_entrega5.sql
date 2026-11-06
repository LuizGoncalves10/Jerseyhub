USE jerseyhub;

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

/*
Justificativa: Funcao que classifica o cliente em uma categoria de fidelidade
de acordo com o total gasto em pedidos: Sem compras, Bronze (menos de R$ 500),
Prata (a partir de R$ 500) ou Ouro (a partir de R$ 900). Centraliza a regra em
um unico lugar, ajudando a loja a direcionar promocoes e a reconhecer os
clientes que mais compram. Usa estrutura condicional (IF / ELSEIF / ELSE) e
retorna NULL quando o cliente nao existe.
*/

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

/*
Justificativa: Funcao que retorna o total de pecas vendidas de uma camisa,
somando as quantidades de todos os itens de pedido em que ela aparece. Mede a
demanda de cada produto, informacao usada para definir quais camisas tem
prioridade na reposicao de estoque. Retorna 0 quando a camisa nunca foi vendida.
*/

DELIMITER ;

-- Exemplos de uso:
-- SELECT nome, fn_classificar_cliente(id_cliente) AS Categoria FROM Cliente;
-- SELECT id_camisa, modelo, fn_total_vendido_camisa(id_camisa) AS Total_Vendido FROM Camisa ORDER BY Total_Vendido DESC;
