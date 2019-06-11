## ${projectName}
---
resources:

  - name: code-repository
    type: git
    source:
      uri: ${gitRepositoryUrl}
      branch: master

jobs:
  - name: build
    plan:
      - get: code-repository
        trigger: true
      - task: build and test
        config:
          platform: linux
          image_resource:
            type: docker-image
            source:
              repository: openjdk
              tag: '8-jdk'
          inputs:
            - name: code-repository
          run:
            path: bash
            args:
              - -exc
              - |
                cd code-repository
                ./gradlew test

## end of pipeline
