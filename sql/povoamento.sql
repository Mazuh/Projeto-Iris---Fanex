﻿-- TABELA hierarquias ----------------------
INSERT INTO hierarquias(nome) VALUES('admin');
INSERT INTO hierarquias(nome) VALUES('instrutor');
INSERT INTO hierarquias(nome) VALUES('aluno');


-- TABELA usuarios --------------------------
-- administradores
INSERT INTO usuarios(id, id_hierarquia, nome, senha)
	VALUES(90001, 1, 'Ale', 'senhaultrasecreta');

-- instrutores
INSERT INTO usuarios(id, id_hierarquia, nome, senha)
	VALUES(90002, 2, 'Luciana', 'pinguelo');
	
INSERT INTO usuarios(id, id_hierarquia, nome, senha)
	VALUES(90003, 2, 'Mayara', 'bolotascheias');
