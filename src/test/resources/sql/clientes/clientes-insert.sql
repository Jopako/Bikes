insert into USUARIOS (id, username, password, role)
    values (200, 'cliente1@gmail.com', '$2b$10$gnDCbB0sgAa9Yn9EAqU9au5o6mojoNRtRwO1UhDYi3cuhh8WFnLi.', 'ROLE_CLIENTE');
insert into USUARIOS (id, username, password, role)
    values (201, 'admin@gmail.com', '$2b$10$gnDCbB0sgAa9Yn9EAqU9au5o6mojoNRtRwO1UhDYi3cuhh8WFnLi.', 'ROLE_ADMIN');
insert into USUARIOS (id, username, password, role)
    values (202, 'cliente2@gmail.com', '$2b$10$gnDCbB0sgAa9Yn9EAqU9au5o6mojoNRtRwO1UhDYi3cuhh8WFnLi.', 'ROLE_CLIENTE');
insert into CLIENTES (id, nome, cpf, usuario_id)
    values (100, 'Maria Silva', '12345678901', 202);
