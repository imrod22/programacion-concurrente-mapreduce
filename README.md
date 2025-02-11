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

###### Configuracion de varios nodes:

```

version: "3.8"
services:
  namenode:
    image: bde2020/hadoop-namenode:2.0.0-hadoop3.2.1-java8
    container_name: namenode
    networks:
      - hadoop-net
    environment:
      - CLUSTER_NAME=test-cluster
    env_file:
      - ./hadoop.env
    ports:
      - "9870:9870"  # UI de NameNode
      - "9000:9000"
    volumes:
      - namenode_data:/hadoop/dfs/name
  datanode1:
    depends_on:
      - namenode
    image: bde2020/hadoop-datanode:2.0.0-hadoop3.2.1-java8
    container_name: datanode1
    networks:
      - hadoop-net
    environment:
      SERVICE_PRECONDITION: "namenode:9000"
    env_file:
      - ./hadoop.env
    ports:
      - "9864:9864"  # UI de DataNode 1
    volumes:
      - datanode1_data:/hadoop/dfs/data
  datanode2:
    depends_on:
      - namenode
    image: bde2020/hadoop-datanode:2.0.0-hadoop3.2.1-java8
    container_name: datanode2
    networks:
      - hadoop-net
    environment:
      SERVICE_PRECONDITION: "namenode:9000"
    env_file:
      - ./hadoop.env
    volumes:
      - datanode2_data:/hadoop/dfs/data
  resourcemanager:
    depends_on:
      - namenode
      - datanode1
      - datanode2
    image: bde2020/hadoop-resourcemanager:2.0.0-hadoop3.2.1-java8
    container_name: resourcemanager
    networks:
      - hadoop-net
    environment:
      SERVICE_PRECONDITION: "namenode:9000 datanode1:9864 datanode2:9864"
    env_file:
      - ./hadoop.env
    ports:
      - "8089:8088"  # UI de YARN
  nodemanager1:
    depends_on:
      - resourcemanager
    image: bde2020/hadoop-nodemanager:2.0.0-hadoop3.2.1-java8
    container_name: nodemanager1
    networks:
      - hadoop-net
    environment:
      SERVICE_PRECONDITION: "resourcemanager:8088"
    env_file:
      - ./hadoop.env
  nodemanager2:
    depends_on:
      - resourcemanager
    image: bde2020/hadoop-nodemanager:2.0.0-hadoop3.2.1-java8
    container_name: nodemanager2
    networks:
      - hadoop-net
    environment:
      SERVICE_PRECONDITION: "resourcemanager:8088"
    env_file:
      - ./hadoop.env
  historyserver:
    depends_on:
      - resourcemanager
      - namenode
      - datanode1
      - datanode2
    image: bde2020/hadoop-historyserver:2.0.0-hadoop3.2.1-java8
    container_name: historyserver
    networks:
      - hadoop-net
    environment:
      SERVICE_PRECONDITION: "namenode:9000 datanode1:9864 datanode2:9864 resourcemanager:8088"
    env_file:
      - ./hadoop.env
    ports:
      - "8188:8188"  # UI de History Server
    volumes:
      - historyserver_data:/hadoop/yarn/timeline
volumes:
  namenode_data:
  datanode1_data:
  datanode2_data:
  historyserver_data:
networks:
  hadoop-net:
    driver: bridge

```

**Extra Dependencies:**

|           Library           |                                                                                                                     Description                                                                                                                     |
| :--------------------------: | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------: |
| Apache Hadoop MapReduce Core | Framework para escribir fácilmente aplicaciones que procesan grandes cantidades de datos (conjuntos de datos de varios terabytes) en paralelo en grandes grupos (miles de nodos) de hardware básico de una manera confiable y tolerante a fallas. |
|           OpenNLP           |                                                                      Kit de herramientas de aprendizaje automático para el procesamiento de texto en lenguaje natural en NLP.                                                                      |
|           CoreNLP           |                                                                                       Kit de herramientas de análisis de lenguaje natural escritas en Java.                                                                                       |
|          JavaVader          |                                        VADER (Valence Aware Dictionary and Sentiment Reasoner) es una herramienta de análisis de sentimientos basada en léxico y reglas expresadas en las redes sociales.                                        |
|         Lucene Core         |                                                             Funciones de indexación, búsqueda y corrección ortográfica, junto con capacidades avanzadas de análisis/tokenización                                                             |
