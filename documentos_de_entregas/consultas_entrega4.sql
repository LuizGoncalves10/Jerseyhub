-- 1. Funcionarios Sem Vendas (Anti Join)
SELECT f.nome AS Funcionario, f.cargo AS Cargo
FROM Funcionario f
LEFT JOIN Pedido p ON f.id_funcionario = p.fk_id_funcionario
WHERE p.id_pedido IS NULL;

-- 2. Historico Completo de Funcionarios e Pedidos (Full Outer Join)
SELECT f.nome AS Funcionario, p.id_pedido AS Pedido, p.valor_total AS Valor
FROM Funcionario f
LEFT JOIN Pedido p ON f.id_funcionario = p.fk_id_funcionario
UNION
SELECT f.nome AS Funcionario, p.id_pedido AS Pedido, p.valor_total AS Valor
FROM Funcionario f
RIGHT JOIN Pedido p ON f.id_funcionario = p.fk_id_funcionario;

-- 3. Funcionarios Que Sao Supervisores (Subconsulta)
SELECT f.nome AS Funcionario, f.cargo AS Cargo
FROM Funcionario f
WHERE f.id_funcionario IN (SELECT fk_id_supervisor FROM Supervisao);

-- 4. Vendedor Responsavel Pela Maior Venda (Subconsulta)
SELECT f.nome AS Vendedor, p.id_pedido AS Pedido, p.valor_total AS Total_Venda
FROM Pedido p
JOIN Funcionario f ON p.fk_id_funcionario = f.id_funcionario
WHERE p.valor_total = (SELECT MAX(valor_total) FROM Pedido);