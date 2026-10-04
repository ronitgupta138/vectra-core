<div align="center">

```text
██╗   ██╗███████╗ ██████╗████████╗██████╗  █████╗      ██████╗ ██████╗ ██████╗ ███████╗
██║   ██║██╔════╝██╔════╝╚══██╔══╝██╔══██╗██╔══██╗    ██╔════╝██╔═══██╗██╔══██╗██╔════╝
██║   ██║█████╗  ██║        ██║   ██████╔╝███████║    ██║     ██║   ██║██████╔╝█████╗  
╚██╗ ██╔╝██╔══╝  ██║        ██║   ██╔══██╗██╔══██║    ██║     ██║   ██║██╔══██╗██╔══╝  
 ╚████╔╝ ███████╗╚██████╗   ██║   ██║  ██║██║  ██║    ╚██████╗╚██████╔╝██║  ██║███████╗
  ╚═══╝  ╚══════╝ ╚═════╝   ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝     ╚═════╝ ╚═════╝ ╚═╝  ╚═╝╚══════╝
```

### **Ultra-Fast In-Memory Vector Search Engine & HNSW Graph Index in Java 21**

[![Java](https://img.shields.io/badge/Java-21%20LTS-0891b2?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-10b981?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Virtual Threads](https://img.shields.io/badge/Loom-Virtual%20Threads-0891b2?style=flat-square&logo=java&logoColor=white)](https://openjdk.org/projects/loom/)
[![Throughput](https://img.shields.io/badge/Throughput-16%2C900%2B%20QPS-10b981?style=flat-square)](https://github.com/ronitgupta138/vectra-core)
[![Latency p50](https://img.shields.io/badge/Latency%20p50-0.28%20ms-0891b2?style=flat-square)](https://github.com/ronitgupta138/vectra-core)
[![Tests](https://img.shields.io/badge/Tests-16%2F16%20Passed-10b981?style=flat-square)](https://github.com/ronitgupta138/vectra-core)

</div>

---

## 📌 Overview

**Vectra Core** is an ultra-fast, zero-native-dependency in-memory vector database and Approximate Nearest Neighbor (ANN) search engine built in **Java 21 (Project Loom Virtual Threads)**. It implements the **Hierarchical Navigable Small World (HNSW)** graph indexing algorithm alongside **8-bit Scalar Quantization (SQ8)** and **Metadata Predicate Filtering**, delivering sub-millisecond query latencies and over 16,000 QPS on high-dimensional embedding workloads (128-dim to 1536-dim).

---

## 🏗️ System Architecture

```
                             [ High-Dimensional Embeddings ]
                           (OpenAI 1536d / ResNet 2048d / MiniLM 384d)
                                           │
                                           ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│                             DISTANCE METRICS & COMPRESSION                       │
│  ├── SIMD-Friendly Vector Operations (Cosine, Euclidean L2, Inner Product)       │
│  └── 8-Bit Scalar Quantization (SQ8): 75% RAM reduction (Float32 ──► Int8)       │
└──────────────────────────────────────────────────────────────────────────────────┘
                                           │
                                           ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│                         HIERARCHICAL NAVIGABLE SMALL WORLD                       │
│                                                                                  │
│   Layer 2 (Sparse):    [Node A] ──────────────────────────► [Node Z]             │
│                            │                                    │                │
│   Layer 1 (Medium):    [Node A] ────────► [Node M] ────────► [Node Z]            │
│                            │                 │                  │                │
│   Layer 0 (Dense):     [Node A] ─► [B] ─► [Node M] ─► [N] ─► [Node Z]            │
│                                                                                  │
│  - Greedy routing across upper skip layers down to entry points                  │
│  - Dynamic candidate queue of size `efSearch` on layer 0                         │
│  - In-flight metadata predicate filtering (category, price, tags)                │
└──────────────────────────────────────────────────────────────────────────────────┘
                                           │
                                           ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│                    HIGH-CONCURRENCY SERVICE LAYER (Java 21 Loom)                 │
│  - Non-blocking virtual thread per search query                                  │
│  - Dynamic batch vector ingestion with thread-safe ReadWriteLock                 │
│  - Multi-collection registry with isolated dimensions and metrics                │
└──────────────────────────────────────────────────────────────────────────────────┘
                                           │
                                           ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│                                   REST API                                       │
│  POST /api/v1/collections                    ──► Create isolated collection      │
│  POST /api/v1/collections/{name}/insert      ──► Single vector insertion         │
│  POST /api/v1/collections/{name}/batch-insert──► High-throughput bulk ingest     │
│  POST /api/v1/collections/{name}/search      ──► Filtered KNN vector retrieval   │
│  GET  /api/v1/system/stats                   ──► JVM heap & collection metrics   │
└──────────────────────────────────────────────────────────────────────────────────┘
```

---

## ⚡ Concurrency & Benchmark Results

Benchmarked on **AMD Ryzen 5 5600H** running 1,000 concurrent vector search requests across **2,000 indexed 128-dimensional normalized visual embeddings** using Java 21 Project Loom virtual threads (`Executors.newVirtualThreadPerTaskExecutor()`):

| Metric | Target SLA | Benchmark Result | Status |
| :--- | :--- | :--- | :--- |
| **Throughput** | > 10,000 QPS | **16,949.15 QPS** | Verified ✅ |
| **Latency p50 (Median)** | < 1.00 ms | **0.283 ms (283 µs)** | Verified ✅ |
| **Latency p95** | < 5.00 ms | **3.286 ms** | Verified ✅ |
| **Latency p99** | < 10.00 ms | **6.346 ms** | Verified ✅ |
| **Recall@5** | > 90% | **> 92.5%** | Verified ✅ |
| **Quantization Memory Ratio** | 4x compression | **75% reduction (Float32 to Int8)** | Verified ✅ |

---

## 🛠️ Key Technical Modules

### 1. Pure Java 21 HNSW Graph (`HnswGraphIndex`)
- Multi-layer skip-graph with logarithmic scaling: $l = \lfloor -\ln(\text{uniform}(0, 1)) \cdot m_L \rfloor$.
- Dynamic beam search on Layer 0 with priority queues bounded by $efSearch$ and $efConstruction$.
- Bi-directional edge assignment with connection pruning ensuring bounded maximum degree $M_0$ and $M$.

### 2. Distance Computation Engine (`DistanceFunction`)
- Hand-crafted loops written for HotSpot C2 autovectorization (SIMD) on AVX2 architectures.
- Exact Cosine, Euclidean $L_2$, and Dot Product similarity functions with automatic numerical clamping.

### 3. Scalar Quantization (SQ8) (`ScalarQuantizer`)
- Compresses 4-byte Float32 components into 1-byte unsigned values using min-max affine mapping:
  $$q_i = \text{round}\left(255 \cdot \frac{x_i - \min}{\max - \min}\right)$$
- Unpacks on cache-lines or computes approximate distances directly in quantized space.

### 4. Hybrid Filtered Search
- Integrates metadata payload filtering directly during Layer 0 graph exploration, skipping invalid candidates before distance calculation.

---

## 📡 API Reference & Curl Examples

### 1. Create a Vector Collection
**`POST /api/v1/collections`**

```bash
curl -X POST http://localhost:8081/api/v1/collections \
  -H "Content-Type: application/json" \
  -d '{
    "name": "product-embeddings",
    "dimension": 128,
    "metric": "COSINE",
    "m": 16,
    "efConstruction": 64,
    "efSearch": 32,
    "useQuantization": false
  }'
```

### 2. Insert Vectors
**`POST /api/v1/collections/{name}/insert`**

```bash
curl -X POST http://localhost:8081/api/v1/collections/product-embeddings/insert \
  -H "Content-Type: application/json" \
  -d '{
    "id": "item_4091",
    "vector": [0.032, -0.114, 0.452, ..., 0.089],
    "metadata": {
      "category": "footwear",
      "brand": "Adidas",
      "price": 89.99
    }
  }'
```

### 3. Approximate Nearest Neighbor Search
**`POST /api/v1/collections/{name}/search`**

```bash
curl -X POST http://localhost:8081/api/v1/collections/product-embeddings/search \
  -H "Content-Type: application/json" \
  -d '{
    "vector": [0.031, -0.110, 0.450, ..., 0.085],
    "k": 5,
    "ef": 32,
    "filter": {
      "category": "footwear"
    }
  }'
```

```json
{
  "collection": "product-embeddings",
  "k": 5,
  "returned": 1,
  "results": [
    {
      "id": "item_4091",
      "distance": 0.0012,
      "similarity": 0.9988,
      "metadata": {
        "category": "footwear",
        "brand": "Adidas",
        "price": 89.99
      }
    }
  ],
  "executionTimeMs": 0.312
}
```

---

## 🚀 Quickstart

```bash
# Clone repository
git clone https://github.com/ronitgupta138/vectra-core.git
cd vectra-core

# Execute complete test suite and benchmark
./mvnw clean test

# Build executable fat JAR
./mvnw clean package

# Run service
./mvnw spring-boot:run
```

---

## 📜 License

Licensed under the [MIT License](LICENSE).
