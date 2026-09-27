-- 1. Clientes TOP
SELECT cl.nome AS Cliente, SUM(p.valor_total) AS Total_Gasto FROM Cliente cl JOIN Pedido p ON cl.id_cliente = p.fk_id_cliente GROUP BY cl.id_cliente, cl.nome HAVING SUM(p.valor_total) > (SELECT AVG(valor_total) FROM Pedido);

-- 2. Acima da Media
SELECT COALESCE(e.nome, 'Sem Equipe') AS Equipe, c.versao AS Versao, c.preco AS Preco FROM Camisa c LEFT JOIN Equipe e ON c.fk_id_equipe = e.id_equipe WHERE c.preco > (SELECT AVG(preco) FROM Camisa);

-- 3. Relatorio
SELECT p.id_pedido AS Pedido, cl.nome AS Cliente, p.data_compra AS Data, p.valor_total AS Total FROM Pedido p JOIN Cliente cl ON p.fk_id_cliente = cl.id_cliente;

-- 4. Estoque
SELECT COALESCE(e.nome, 'Sem Equipe') AS Equipe, c.versao AS Versao, c.tamanho as Tamanho, c.quantidade_estoque AS Estoque FROM Camisa c LEFT JOIN Equipe e ON c.fk_id_equipe = e.id_equipe ORDER BY c.quantidade_estoque DESC;

-- 5. Clientes Frequentes
SELECT cl.nome AS Cliente, COUNT(p.id_pedido) AS Total_Pedidos FROM Cliente cl JOIN Pedido p ON cl.id_cliente = p.fk_id_cliente GROUP BY cl.id_cliente, cl.nome HAVING COUNT(p.id_pedido) > 0 ORDER BY Total_Pedidos DESC;

-- 6. Camisas Sem Venda
SELECT c.modelo AS Modelo, c.versao AS Versao, c.preco AS Preco FROM Camisa c WHERE c.id_camisa NOT IN (SELECT fk_id_camisa FROM Item_Pedido);

-- 7. Ticket Medio
SELECT cl.nome AS Cliente, AVG(p.valor_total) AS Ticket_Medio FROM Cliente cl JOIN Pedido p ON cl.id_cliente = p.fk_id_cliente GROUP BY cl.id_cliente, cl.nome ORDER BY Ticket_Medio DESC;

-- 8. Camisa Mais Cara
SELECT e.nome AS Equipe, c.modelo AS Modelo, c.preco AS Preco FROM Camisa c JOIN Equipe e ON c.fk_id_equipe = e.id_equipe WHERE c.preco = (SELECT MAX(preco) FROM Camisa c2 WHERE c2.fk_id_equipe = e.id_equipe);

-- 9. Clientes Sem Compras
SELECT cl.nome AS Cliente, cl.cpf AS CPF FROM Cliente cl WHERE cl.id_cliente NOT IN (SELECT fk_id_cliente FROM Pedido);