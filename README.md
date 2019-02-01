# Spring Cache Benchmarks

Here you will find benchmarks to compare cache performance, with and
without Spring, with different strategies in Spring (proxies and
AspectJ), and with different cache types (EhCache and in-memory maps).

Summary: 

* With a "real" cache (EhCache in this case) Spring cache interceptor
can outperform the manual implementation.

* The performance hit of the cache itself washes out any differences.

* With a "simple" cache Spring is actually slower (by a factor of 20),
but it has more complicated logic than the manual implementation.

* A proxy is a bit slower than AspectJ, but not much.

## Running the Benchmarks

The `CacherBenchmarkIT` can be run from an IDE and it prints a
benchmark result. You can also run on the command line with `mvn
integration-test -DbenchmarksEnabled=true`. The project is set up to
run the EhCache, proxy version out of the box.

To run with AspectJ put this on the command line in the JVM launcher:
`-javaagent:${system_property:user.home}/.m2/repository/org/aspectj/aspectjweaver/1.9.2/aspectjweaver-1.9.2.jar`.

To run with the "simple" cache use `spring.cache.type=simple`
(e.g. uncomment the line in `application.properties`).

## Results

With EhCache and proxies:

```
Benchmark                (sample)   Mode  Cnt       Score       Error  Units
CacherBenchmarkIT.bench    manual  thrpt   10  119913.510 ± 16544.001  ops/s
CacherBenchmarkIT.bench       raw  thrpt   10      99.149 ±     0.223  ops/s
CacherBenchmarkIT.bench    spring  thrpt   10  147824.696 ± 17184.719  ops/s
```

and AspectJ:

```
Benchmark                (sample)   Mode  Cnt       Score       Error  Units
CacherBenchmarkIT.bench    manual  thrpt   10  125094.198 ±  8833.210  ops/s
CacherBenchmarkIT.bench       raw  thrpt   10      99.130 ±     0.087  ops/s
CacherBenchmarkIT.bench    spring  thrpt   10  153632.777 ± 11129.739  ops/s
```

With `spring.cache.type=simple` and proxies:

```
Benchmark                (sample)   Mode  Cnt         Score         Error  Units
CacherBenchmarkIT.bench    manual  thrpt   10  18640870.390 ± 1971217.736  ops/s
CacherBenchmarkIT.bench       raw  thrpt   10        99.171 ±       0.081  ops/s
CacherBenchmarkIT.bench    spring  thrpt   10    916481.770 ±   74530.962  ops/s
```

and AspectJ:

```
Benchmark                (sample)   Mode  Cnt         Score         Error  Units
CacherBenchmarkIT.bench    manual  thrpt   10  18477865.719 ± 1702046.330  ops/s
CacherBenchmarkIT.bench       raw  thrpt   10        99.169 ±       0.131  ops/s
CacherBenchmarkIT.bench    spring  thrpt   10   1064482.744 ±  122944.023  ops/s
```
