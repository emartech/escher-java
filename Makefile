.PHONY: test sh

test:
	docker-compose run --rm app mvn test

sh:
	docker-compose run --rm app bash

bundle:
	docker-compose run --rm app ./bundle.sh

