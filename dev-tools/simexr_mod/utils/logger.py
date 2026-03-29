# delete the failing import
# from utils.logger import setup_logging

import logging

def setup_logging(name: str):
    logging.basicConfig(level=logging.INFO,
                        format="%(asctime)s %(levelname)s %(message)s")
    return logging.getLogger(name)

logger = setup_logging("sandbox_executor")
