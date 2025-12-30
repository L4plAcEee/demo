import logging
from sqlalchemy import Column, Integer, String, create_engine
from sqlalchemy.orm import declarative_base, sessionmaker

Base = declarative_base()

class User(Base):
    __tablename__ = 'users'
    id = Column(Integer, primary_key=True)
    name = Column(String, unique=True, nullable=False)

    def __repr__(self):
        return f"<User(name={self.name})>"

engine = create_engine("sqlite:///example.db")
Session = sessionmaker(bind=engine)
session = Session()

Base.metadata.create_all(engine)  # 创建表

session.add(User(name="Alice")) if session.query(User).filter_by(name="Alice").first() is None else None
session.add(User(name="Bob")) if session.query(User).filter_by(name="Bob").first() is None else None

logging.basicConfig(level=logging.INFO)
logging.info(session.query(User).all())
session.commit()
