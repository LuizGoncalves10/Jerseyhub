import mysql.connector


def conectar():
    conexao = mysql.connector.connect(
        host="localhost",
        user="root",
        password="sua_senha",
        database="jerseyhub"
    )

    return conexao