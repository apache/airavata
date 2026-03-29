from dataclasses import dataclass
from typing import Optional
from contextlib import contextmanager
import sqlite3
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, Session
from sqlalchemy.engine import Engine

@dataclass
class DatabaseConfig:
    dialect: str = "sqlite"
    database_path: str = "mcp.db"
    echo: bool = False
    host: Optional[str] = None
    port: Optional[int] = None
    username: Optional[str] = None
    password: Optional[str] = None
    _engine: Optional[Engine] = None
    _session_factory: Optional[sessionmaker] = None

    @property
    def connection_string(self) -> str:
        if self.dialect == "sqlite":
            return f"sqlite:///{self.database_path}"
        elif self.dialect == "postgresql":
            return f"postgresql://{self.username}:{self.password}@{self.host}:{self.port}/{self.database_path}"
        raise ValueError(f"Unsupported dialect: {self.dialect}")

    def get_engine(self) -> Engine:
        if self._engine is None:
            self._engine = create_engine(self.connection_string, echo=self.echo)
        return self._engine

    def get_session_factory(self) -> sessionmaker:
        if self._session_factory is None:
            self._session_factory = sessionmaker(bind=self.get_engine())
        return self._session_factory

    @contextmanager
    def get_session(self) -> Session:
        session = self.get_session_factory()()
        try:
            yield session
            session.commit()
        except Exception:
            session.rollback()
            raise
        finally:
            session.close()

    @contextmanager
    def get_sqlite_connection(self) -> sqlite3.Connection:
        """Get a SQLite connection with row factory set to dict-like rows"""
        conn = sqlite3.connect(str(self.database_path))
        conn.row_factory = sqlite3.Row
        try:
            yield conn
            conn.commit()
        except Exception:
            conn.rollback()
            raise
        finally:
            conn.close()