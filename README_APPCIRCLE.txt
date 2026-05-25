QAxion - Pacote final para AppCircle

Configuração recomendada no AppCircle:
- Module: app
- Variant: debug
- Output Type: APK
- Workflow: Default Push Workflow

Este pacote força Gradle 8.7 pelo arquivo gradlew para evitar erro de incompatibilidade com Gradle 9.1:
Cannot mutate the dependencies of configuration ':app:debugCompileClasspath'
