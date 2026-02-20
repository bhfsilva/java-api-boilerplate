# Boilerplate

Boilerplate Java para backend simples.

---

## Como executar o projeto localmente?

1. Instale o [Docker](https://docs.docker.com/engine/install/), caso ainda não esteja instalado;
2. Crie um arquivo **.env** minímo na raiz do projeto:

```
PROFILE=local
VERSION=0.0.0-LOCAL
DB_NAME=boilerplate-db
DB_USERNAME=<DB_USERNAME>
DB_PASSWORD=<DB_PASSWORD>
```

>💡 Substitua os valores pelos específicos do projeto.

3. Execute o comando `docker compose --env-file .env up -d` para iniciar os contêineres da aplicação baseado nas definições do arquivo **docker-compose.yml**.

>📥 **Dependências**:
>
> [PostgreSQL](https://www.postgresql.org/docs/current/) para armazenamento de dados;
>
> [Azurite](https://github.com/Azure/Azurite) para armazenamento de arquivos.

## Iniciando um projeto (opcional)

Ao iniciar um novo projeto, utilize o parâmetro `--to` do script **build.py** para nomeá-lo:

```bash
python3 build.py --to <PROJECT_NAME>
```

>💡 Substitua os valores pelos específicos do projeto.

Use o parâmetro `--from` para renomear um projeto:

```bash
python3 build.py --from <OLD_NAME> --to <NEW_NAME>
```

>📥 **Requisitos**:
>
> [Python](https://www.python.org/downloads/) esteja instalado;
>
> É preferível que os valores de `<OLD_NAME>` e `<NEW_NAME>` estejam em letra minúscula e separados por espaços.

## Criação de entidades

Com o parâmetro `--add` do script **build.py** é possível criar arquivos relacionadas a uma entidade:

```bash
python3 build.py --add <ENTITY_NAME>
```

>💡 É preferível que o valor de `<ENTITY_NAME>` esteja em letra minúscula e separado por espaços.

Por exemplo, `<ENTITY_NAME> = 'user'` resultará nos arquivos:

```
src/main/
└─ java/
│  └─ entities/
│  │  └─ User.java *
│  └─ repositories/
│  │  └─ user/ *
│  │    └─ UserRepositoryImpl.java *
│  │    └─ UserJpaRepository.java *
│  ├─ usecases/
│  │  └─ user/ *
│  │     └─ .gitkeep
│  ├─ rest/
│  │  ├─ specs/
│  │  │  └─ UserControllerSpecs.java *
│  │  └─ controllers/
│  │     └─ UserController.java *
│  ├─ dtos/
│  │  └─ user/ *
│  │     └─ .gitkeep
│  └─ mappers/
│     └─ user/ *
│        ├─ UserMapperImpl.java *
│        └─ UserStructMapper.java *
└─ resources/db.migration/
   └─ V1__create_user_table.sql *
```
