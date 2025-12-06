import pandas as pd
bankloan = pd.read_excel('./data/bankloan.xls', engine='xlrd')
print(bankloan.columns.tolist())
