# Code Standards

For the Python code we follow the
[PEP8 standard](https://www.python.org/dev/peps/pep-0008/). A linter called
`flake8` is used to verify adherence to the standard.

## Setting up dev environment

```bash
source venv/bin/activate
pip install -r requirements-dev.txt
```

## Running flake8

```
flake8 .
```

## Automatically formatting Python code

```
autopep8 -i -aaa -r .
isort .
```
