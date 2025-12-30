import logging
from sqlalchemy import Column, Integer, String, create_engine, JSON, Date
from sqlalchemy.orm import declarative_base, sessionmaker
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.sql import func
from sqlalchemy.ext.mutable import MutableList

Base = declarative_base()
logging.basicConfig(level=logging.INFO)

class PunchRecord(Base):
    __tablename__ = 'punch_records'

    id = Column(Integer, primary_key=True, autoincrement=True)
    punch_date = Column(Date, nullable=False)
    # sqlalchemy默认不追踪数据对象的变化， MuatableList 用于追踪可变对象的变化
    tags = Column(MutableList.as_mutable(JSON), nullable=True) 

    def __repr__(self):
        return f"<PunchRecord(punch_date={self.punch_date}, tags={self.tags})>"

engine = create_engine("sqlite:///example.db")
Session = sessionmaker(bind=engine)
session = Session()

Base.metadata.create_all(engine)

def punch_tag(session, punch_date, new_tag):
    record = session.query(PunchRecord).filter_by(punch_date=punch_date).first()
    if record:
        if new_tag not in record.tags:
            record.tags.append(new_tag)
            session.commit()
            return f"打卡成功：{new_tag}"
        else:
            return f"已打卡：{new_tag}（跳过）"
    else:
        new_record = PunchRecord(punch_date=punch_date, tags=[new_tag])
        session.add(new_record)
        session.commit()
        return f"首次打卡：{new_tag}"
date_str = "2023-10-01"
from datetime import datetime
punch_date = datetime.strptime(date_str, '%Y-%m-%d').date()
logging.info(punch_tag(session, punch_date, "早起"))
logging.info(punch_tag(session, punch_date, "跑步"))
logging.info(punch_tag(session, punch_date, "早起"))

logging.info(session.query(PunchRecord).all())

session.commit()
