from banco import conectar


conexao = conectar()
cursor = conexao.cursor()

sql = """
SELECT id_cliente, nome, cpf, bairro
FROM Cliente;
"""

cursor.execute(sql)

resultados = cursor.fetchall()

for cliente in resultados:
    print(cliente)

cursor.close()
conexao.close()