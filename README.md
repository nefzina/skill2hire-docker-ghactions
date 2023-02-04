# Continuous Integration with GitHub Action

Our goal is to implement this user story: 

> As a developer, I want my application to be built **if I push new code** and all tests pass. The application should be deployable after the built, **so I can test my changes immediately**.

To test our changes, we have to set up the infrastructure with `docker compose up` and, if we want to use the reverse proxy, the NGINX Docker container as well. Consult the last quest if the setup does not work.

## 👉 Target Architecture

As a result of this quest, this **build pipeline** should be implemented.

![](https://i.imgur.com/Z6R2qCCl.png)

We have these components already up and running:

* Maven Build Process
* Spring Boot Maven Docker Plugin

This enables us to create a new Docker image with `mvn spring-boot:build-image`.

We also have to pulling step implemented with Docker Compose:

* Pull Docker image for Docker Compose

If you call `docker-compose up`, the images `ice0nine/docker-demo:staging` and `ice0nine/docker-demo:prod` will be pulled at DockerHub and be started in a local Docker Compose environment (cmp. last quest).

So what is missing? We need a trigger which, once we push new code to build the application on a build server, runs all tests and creates a runnable artifact (the Spring Boot web application in our case).

## 📖 Setup GitHub Actions

As we use GitHub as our code repository, it makes sense to let GitHub provide the **Continuous Integration** we want to implement. GitHub does this with **GitHub Actions**.

### Configuring GitHub Actions for Java & Maven

We will use this GitHub Actions config for our build:

```yaml
name: Java CI

on: [push]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 19
        uses: actions/setup-java@v3
        with:
          java-version: '19'
          distribution: 'temurin'
      - name: Build with Maven
        run: mvn --batch-mode --update-snapshots spring-boot:build-image -Dimage.tag=${GITHUB_REF##*/}
      - name: Login to DockerHub
        uses: docker/login-action@v1
        with:
          username: ${{ secrets.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_TOKEN }}
      - name: Push
        run: |
          docker push ice0nine/docker-demo:${GITHUB_REF##*/}
```

```resource
https://docs.github.com/en/actions
# GitHub Actions Reference
Read more about GitHub Actions and the commands available in the config.
```

### 🔒 Configure Secrets

> We don't want out credentials for Docker to appear anywhere in our code base. Therefore, we make sure to set the credentials in a vault provided by GitHub for these use cases.


To be able to reference the secrets in the workflow definition like this

```yaml
username: ${{ secrets.DOCKERHUB_USERNAME }}
password: ${{ secrets.DOCKERHUB_TOKEN }}
```

we have to store them with the correct keys in the Actions secrets and variables.

![](https://i.imgur.com/K1GxqPsl.png)

### 👉 GitHub Actions Workflow Definition

In our `.github/workflows/on-push.yml` file (_note that the repository folder `.github/workflow` is a convention and must be used for GitHub Actions to work_).

On github.com in your repository, you can choose the tab **Actions** if the file exists and is accessible to GitHub. 

With the instruction `on: [push]` GitHub is instructed to run the actions on each `git push` to any branch. **Our goal is that the Docker image is generated after each push and successful unit testing and pushed to DockerHub.**

After a push you can watch the process on the GitHub Actions tab for this build.

![](https://i.imgur.com/qOKdZW4l.png)

You can see each step with its **name** and the execution result.

In our `on-push.yml`, these steps are defined:

* actions/checkout@v3 (not named)
* Set up JDK 19
* Build with Maven
* Login to DockerHub
* Push

You can drill down into logs of each step during the build and watch the execution:

![](https://i.imgur.com/y46bkaVl.png)

After a successful build, the green checkmark should be assigned to the build. 
If there are errors, you can investigate the logs to find our the reasons.

![](https://i.imgur.com/4cRwdG7l.png)

After successful build, you can log into docker.hub.com to see if the images have been pushed correctly. 
If they have, these new images are now accessible for all users, eg. our `docker-compose.yml`.

![](https://i.imgur.com/yjExxXfl.png)
