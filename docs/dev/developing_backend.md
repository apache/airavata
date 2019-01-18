# Code Standards

For the Python code we follow the
[PEP8 standard](https://www.python.org/dev/peps/pep-0008/). A linter called
`flake8` is used to verify adherence to the standard.

## Running flake8

```bash
source venv/bin/activate
pip install -r requirements-dev.txt
flake8 --ignore=E501 .
```
