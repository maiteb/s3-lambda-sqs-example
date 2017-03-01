# s3-lambda-sqs-example

Este repositório contém o passo a passo para criação de um projeto JAVA,
que será executado em no AWS Lambdam  que recebe um evento do s3,
executa uma função X e depois envia a saída para uma fila do
SQS.

## Passos para criação de um artefato para o AWS Lambda

- Vamos criar um projeto maven padrão, utilizando o `archetype` básico. Para isso, execute o seguinte comando:

```sh
 mvn -B archetype:generate \
  -DarchetypeGroupId=org.apache.maven.archetypes \
  -DgroupId=the.group.id \
  -DartifactId=the.artifact.id
```

- Adicione a seguinte dependência no `pom.xml` gerado, para criar uma
   função a ser executada no AWS Lambda:

```xml
<dependencies>
    <dependency>
      <groupId>com.amazonaws</groupId>
      <artifactId>aws-lambda-java-core</artifactId>
      <version>1.1.0</version>
    </dependency>
</dependencies>
```

- Adicione o seguinte plugin no `pom.xml` gerado:

```
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-shade-plugin</artifactId>
  <configuration>
    <createDependencyReducedPom>false</createDependencyReducedPom>
  </configuration>
  <executions>
    <execution>
      <phase>package</phase>
      <goals>
        <goal>shade</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

- Crie uma classe que implemente `RequestHandler` e apenas escreva uma
   mensagem no log.

- Crie um pacote ao rodar `mvn package`

- Suba o pacote no AWS Lambda que você já deve ter criado.

- Execute a função e veja nos logs o resultado
