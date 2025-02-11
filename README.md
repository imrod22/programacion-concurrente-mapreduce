## Programacion Concurrente - MapReduce.

## Descripción

Trabajo complementario sobre el Modelo MapReduce.

## Autor

- [@Imanol Morales](https://www.github.com/imrod22) - 8276/9

## Tarea

#### Este estudio se centra en el procesamiento de datos utilizando el modelo de MapReduce, comenzando con una profundización en su modelo conceptual. Se aborda el propósito original de MapReduce, qué necesidades específicas busca resolver, cómo se estructura su flujo de procesamiento interno, y cuál es la arquitectura que lo sustenta.


## Jar y Inputs de las pruebas realizadas

Benchmark: /archivos_utilizados/benchmarking_bateria/

    Medidas: /archivos_utilizados/benchmarking_bateria/data --version reducida (detalle) y version completa (compress)

Messi X: /archivos_utilzados/messi_x/

    tweets: /archivos_utilizados/data --version detalle (compact) y version completa (tweets_messi)

    dependencias: /archivos_utilizados/libs (falta coreNLP)

## Stack Tecnica

**Client:** Java - Docker Hadoop (https://github.com/big-data-europe/docker-hadoop/tree/master)

**Server:** Docker (https://www.docker.com/products/docker-desktop/)

**Extra Dependencies:**

|           Library           |                                                                                                                     Description                                                                                                                     |
| :--------------------------: | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------: |
| Apache Hadoop MapReduce Core | Framework para escribir fácilmente aplicaciones que procesan grandes cantidades de datos (conjuntos de datos de varios terabytes) en paralelo en grandes grupos (miles de nodos) de hardware básico de una manera confiable y tolerante a fallas. |
|           OpenNLP           |                                                                      Kit de herramientas de aprendizaje automático para el procesamiento de texto en lenguaje natural en NLP.                                                                      |
|           CoreNLP           |                                                                                       Kit de herramientas de análisis de lenguaje natural escritas en Java.                                                                                       |
|          JavaVader          |                                        VADER (Valence Aware Dictionary and Sentiment Reasoner) es una herramienta de análisis de sentimientos basada en léxico y reglas expresadas en las redes sociales.                                        |
|         Lucene Core         |                                                             Funciones de indexación, búsqueda y corrección ortográfica, junto con capacidades avanzadas de análisis/tokenización                                                             |
