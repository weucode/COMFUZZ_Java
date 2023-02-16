import pymysql

sql_config = dict(host='comfort_mysql',
                  port=3306,
                  user='root',
                  passwd='mysql123',
                  db='JVMFuzzing',
                  charset='utf8mb4')
# sql_config = dict(host='10.15.0.37',
#                   port=13000,
#                   user='root',
#                   passwd='mysql123',
#                   db='JVMFuzzing2',
#                   charset='utf8mb4')


class DataBaseHandle(object):

    def __init__(self):
        self.conn = sql_config

    def createTable(self, sql):
        conn = pymysql.connect(**self.conn)
        cur = conn.cursor()
        cur.execute(sql)
        cur.close()
        conn.close()

    def selectOne(self, sql, prames):
        conn = pymysql.connect(**self.conn)

        cur = conn.cursor()
        cur.execute(sql, prames)
        data = cur.fetchone()
        cur.close()
        conn.close()
        return data

    def selectall(self, sql):
        conn = pymysql.connect(**self.conn)

        cur = conn.cursor()
        cur.execute(sql)
        data = cur.fetchall()
        cur.close()
        conn.close()
        return data

    def selectmany(self, sql, prames):
        conn = pymysql.connect(**self.conn)

        cur = conn.cursor()
        cur.execute(sql, prames)
        data = cur.fetchall()
        cur.close()
        conn.close()
        return data

    def insert(self, sql, prames):
        conn = pymysql.connect(**self.conn)

        cur = conn.cursor()
        recount = cur.execute(sql, prames)
        conn.commit()
        cur.close()
        conn.close()
        return recount

    def insertMany(self, sql, lis):
        conn = pymysql.connect(**self.conn)

        cur = conn.cursor()
        recount = cur.executemany(sql, lis)
        conn.commit()
        cur.close()
        conn.close()
        return recount

    def delete(self, sql, prames):
        conn = pymysql.connect(**self.conn)

        cur = conn.cursor()
        recount = cur.execute(sql, prames)
        conn.commit()
        cur.close()
        conn.close()
        return recount

    def deleteAll(self, sql):
        conn = pymysql.connect(**self.conn)

        cur = conn.cursor()
        recount = cur.execute(sql)
        conn.commit()
        cur.close()
        conn.close()
        return recount

    def update(self, sql, prames):
        conn = pymysql.connect(**self.conn)

        cur = conn.cursor()
        recount = cur.execute(sql, prames)
        conn.commit()
        cur.close()
        conn.close()
        return recount
