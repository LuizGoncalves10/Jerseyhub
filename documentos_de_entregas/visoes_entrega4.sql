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

/* 
Justificativa: Visao que cruza as tabelas Equipe Camisa 
Item_Pedido e Pedido para calcular o faturamento e volume de vendas 
de cada time. Isso ajuda a loja a identificar quais equipes geram 
mais lucro e orienta a compra de novos estoques para o dashboard.
*/

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

/* 
Justificativa: Visao que cruza as tabelas Funcionario Pedido 
Item_Pedido e Cliente para avaliar o desempenho da equipe de vendas. 
Ela soma o valor total vendido e a quantidade de clientes atendidos 
por cada vendedor ajudando a gerenciar comissoes e relatorios da loja.
*/