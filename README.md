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

```xml
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

- Crie uma classe que implemente `RequestHandler<Object,Object>` e apenas escreva uma
   mensagem no log.

- Crie um pacote ao rodar `mvn package`

- Suba o pacote no AWS Lambda que você já deve ter criado.

- Execute a função e veja nos logs o resultado

---

## Criando a conexão com o S3

- Adicione as seguintes dependências no `pom.xml`

```xml
<dependency>
			<groupId>com.amazonaws</groupId>
			<artifactId>aws-lambda-java-events</artifactId>
			<version>1.3.0</version>
</dependency>
<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-java-sdk-s3</artifactId>
    <version>1.11.98</version>
</dependency>
```

- Altere a sua classe que implemente `RequestHandler<Object,Object>` para `RequestHandler<com.amazonaws.services.lambda.runtime.events.S3Event, Object>`

- Para consumir um arquivo do S3, utilize a seguinte sequência de
  código:

```java
@Override
    public Object handleRequest(S3Event input, Context context) {
        List<S3EventNotificationRecord> s3EventNotificationRecords =
input.getRecords();

        s3EventNotificationRecords.forEach(event -> processEvent(event,
context.getLogger()));

        return "success";
    }

    private Object processEvent(S3EventNotificationRecord event,
LambdaLogger logger) {
        AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
        S3Object object = s3
            .getObject(new
GetObjectRequest(event.getS3().getBucket().getName(),
event.getS3().getObject().getKey()));

        try (BufferedReader br = new BufferedReader(new
InputStreamReader(object.getObjectContent()))) {
            String line;
            while ((line = br.readLine()) != null) {
                logger.log(line);
            }
        } catch (IOException e) {
            logger.log("Erro ao ler arquivo do s3");
        }

        return null;
    }

```

- Gere um novo pacote e suba no AWS Lambda

- Adicione um trigger para o seu Lambda com as seguintes informações:
  - S3 Event
  - Bucket: __bucket-name__
  - Type: ObjectCreated (all)
  - Enabled: true

---

## Enviando mensagem para o SQS

- Adicione a seguinte dependência no `pom.xml`:

```xml
<dependency>
	<groupId>com.amazonaws</groupId>
	<artifactId>amazon-sqs-java-messaging-lib</artifactId>
	<version>1.0.1</version>
</dependency>
```

- O seguinte trecho de código é capaz de enviar uma mensagem para uma fila do SQS já criada:

```java
   @Override
    public Object handleRequest(S3Event input, Context context) {
        List<S3EventNotificationRecord> s3EventNotificationRecords = input.getRecords();

        String queueName = "NOME DA FILA";
        AmazonSQSClient sqsClient = new AmazonSQSClient();
        String queueUrl = sqsClient.getQueueUrl(new GetQueueUrlRequest(queueName)).getQueueUrl();

				sendToSQS("mensagem teste", sqsClient, queueUrl);

        return "success";
    }

    private void sendToSQS(String message, AmazonSQSClient sqsClient, String queueUrl) {
        SendMessageRequest myMessageRequest = new SendMessageRequest(queueUrl,
            message);
        sqsClient.sendMessage(myMessageRequest);
    }
```
