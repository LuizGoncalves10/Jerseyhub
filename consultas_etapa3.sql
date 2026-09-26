
SELECT 
    e.nome AS Nome_Equipe,
    CASE 
        WHEN e.equipe_tipo = 1 THEN 'Clube'
        ELSE 'Seleção'
    END AS Tipo,
    SUM(ip.subtotal_item) AS Receita_Total
FROM Equipe e
INNER JOIN Camisa c ON e.id_equipe = c.fk_id_equipe
INNER JOIN Item_Pedido ip ON c.id_camisa = ip.fk_id_camisa
GROUP BY e.id_equipe
ORDER BY Receita_Total DESC;

SELECT 
    c.nome AS Nome_Cliente,
    c.cpf AS CPF,
    p.id_pedido,
    p.valor_total
FROM Cliente c
INNER JOIN Pedido p ON c.id_cliente = p.fk_id_cliente
WHERE p.valor_total > (SELECT AVG(valor_total) FROM Pedido)
ORDER BY p.valor_total DESC;

SELECT 
    cl.nome AS Cliente,
    p.data_compra,
    e.nome AS Equipe,
    pers.nome_costas AS Nome_Personalizado,
    pers.numero_costas AS Numero_Personalizado
FROM Personalizacao pers
INNER JOIN Item_Pedido ip ON pers.fk_id_item_pedido = ip.id_item_pedido
INNER JOIN Pedido p ON ip.fk_id_pedido = p.id_pedido
INNER JOIN Cliente cl ON p.fk_id_cliente = cl.id_cliente
INNER JOIN Camisa c ON ip.fk_id_camisa = c.id_camisa
INNER JOIN Equipe e ON c.fk_id_equipe = e.id_equipe;

SELECT 
    e.nome AS Equipe,
    SUM(c.quantidade_estoque) AS Estoque_Total_Camisas,
    COUNT(DISTINCT cc.cor) AS Variedade_Cores
FROM Equipe e
LEFT JOIN Camisa c ON e.id_equipe = c.fk_id_equipe
LEFT JOIN Camisa_Cor cc ON c.id_camisa = cc.fk_id_camisa
GROUP BY e.id_equipe
HAVING Estoque_Total_Camisas > 20
ORDER BY Estoque_Total_Camisas DESC;