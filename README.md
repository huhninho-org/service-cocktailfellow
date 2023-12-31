# Cocktail Fellow Service

## Introduction

Cocktail Fellow API is a comprehensive interface designed to streamline the management of users, groups, and cocktails.
This API, primarily serving the Cocktail Fellow mobile app, offers a user-friendly platform for sharing, managing and
accessing cocktail recipes effectively.

## Table of Contents

- [Features](#features)
- [Authentication](#authentication)
- [Key Technologies](#key-technologies)
- [Secrets Management](#secrets-management)
- [Local Development](#local-development)
- [Contact](#contact)

## Features

- **User management:**
  - _create_ a new user including username and password.
  - Authenticate the user during the _login_ process.
  - _delete_ a user.
- **Group management:**
  - _create_ a group to contain cocktails.
  - _get all groups_ of a user and get all cocktails of a _specific group_.
  - _share a group_ with other users.
  - _delete_ a group.
- **Cocktail management:**
  - _add_ cocktails to a group.
  - _update_ an already existing cocktail.
  - _get all_ cocktails of a group as well as get a _detailed description_ of a cocktail.
  - _delete_ a cocktail from a group.
- **Ingredient-based cocktail filtering:**
  - _get all_ cocktails that contains filtered ingredients.

Find a detailed description of the endpoints in the [API documentation](docs/api/cocktailfellow.openapi.yaml).

## Authentication

Some endpoints require authentication. This API uses JWT tokens for user authentication and API keys for certain
operations.

- **Bearer Authentication:** For endpoints requiring a JWT token, include the token in the Authorization header as a
  Bearer token.
- **API Key Authentication:** For endpoints requiring an API key, include the API key in the request header.

## Key Technologies

The Service uses the following technologies:

- **Kotlin:** The Service is written in [Kotlin](https://kotlinlang.org/), a modern programming language that runs on
  the JVM.
- **Gradle:** The Service uses [Gradle](https://gradle.org/) as a build tool.
- **Github:** The Service is hosted on [Github](https://github.com/).
- **AWS Lambda Function:** The Service is deployed as a set of [AWS Lambda functions](https://aws.amazon.com/lambda/).
- **AWS DynamoDB:** The Service uses [AWS DynamoDB](https://aws.amazon.com/dynamodb/) as a database.
- **Serverless.com:** The Service is deployed using the [Serverless.com](https://www.serverless.com/) framework.

## Secrets Management

Secrets are stored in Serverless.com's [secrets store](https://www.serverless.com/secrets). The secrets are injected as
environment variables into the Lambda functions at deployment time. Therefore, the secrets are not stored in the
repository and are not visible in the code. You will not have access to the secrets store unless you are granted access
by the project owner.

## Local Development

These instructions will get you a copy of the project up and running on your local machine for development and testing
purposes. As long as you don't have access to the secrets store, you should only develop locally. The deployment to AWS
is done by any of the team members.

The guide assumes that you have the required software such as Kotlin and Gradle installed on your machine. Therefore,
only the setup of serverless and a local development environment will be discussed in detail.

### Prerequisites

To get the app running locally, the following packages are required:

##### Serverless

See also the official [Serverless documentation](https://www.serverless.com/framework/docs/getting-started).

```bash
npm install -g serverless
```

##### Serverless offline

See also the official [Serverless documentation](https://www.serverless.com/plugins/serverless-offline).

```bash
npm install serverless-offline
```

##### Serverless dynamodb

See also the official [Serverless documentation](https://www.serverless.com/plugins/serverless-dynamodb-local). Please
note, this service requires a forked version of serverless dynamo db for local development, due to the reasons
described [here](https://github.com/raisenational/serverless-dynamodb#migrating-from-serverless-dynamodb-local).

```bash
npm install serverless-dynamodb
```

### Running the app locally

To run the app locally, you have to build the service and start the serverless offline plugin. Afterwords, you can use
the postman collection to test the endpoints.

##### Build the service

```bash
./gradlew clean build
```

##### Start the serverless offline plugin

```bash
serverless offline start --stage=local
```

##### Test the endpoints

You can use the postman collection to test the endpoints. The collection is located in the [postman](docs/postman)
folder. Please note that you should use the [CocktailFellow local.json](docs/postman/CocktailFellow local.json)
environment file to set the correct environment variables. You can also use the following curl commands to test the
endpoints:

###### Create a user

Create a user to be able to log in.

```bash
curl --location 'http://localhost:3000/local/users' \
--header 'x-api-key: MY-APP-API-KEY' \
--header 'Content-Type: application/json' \
--data '{
    "username": "my-username",
    "password":"my-password"
}'
```

###### Login

Log in with the previously created user.

```bash
curl --location 'http://localhost:3000/local/login' \
--header 'x-api-key: MY-APP-API-KEY' \
--header 'Content-Type: application/json' \
--data '{
    "username": "my-username",
    "password":"my-password"
}'
```

###### Create a group

Use the token from the login response to create a group.

```bash
curl --location 'http://localhost:3000/local/groups' \
--header 'Authorization: Bearer <previously-created-token>' \
--header 'Content-Type: application/json' \
--data '{
    "groupName":"my-group"
}'
```

## Contact

For support or to engage in community discussions, feel free to reach out:

- **Cocktail Fellow GitHub Issues**: Visit
  our [GitHub issues](https://github.com/huhninho/service-cocktailfellow/issues) to report issues.
- **Email Support**: For direct support or inquiries, email us at [huhninho@gmail.com](mailto:huhninho@gmail.com).

We welcome your questions, feedback, and contributions!
