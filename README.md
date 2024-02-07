# Countries
Experiments with kotlin and ktor client

## Building and running

Build and run tests:
```
./gradlew build
```

Build without running tests:
```
./gradlew build -x test
```

Run program (make sure to build first):
```
./countries --help
./countries europe --help
./countries currencies --help
```

List countries in Europe:
```
./countries europe
./countries europe --sort=name:asc
```

List currencies
```
./countries currencies
```
