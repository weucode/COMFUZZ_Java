import os

from src.studyMysql.db_operation_base import DataBaseHandle


class Table_Function(object):

    def __init__(self):
        self.__table = DataBaseHandle()

    def selectOneFromTableFunction(self, id):
        sql = 'select * from Table_Function where id=%s'
        prames = (id)
        return self.__table.selectOne(sql, prames)

    def selectSourceIdFromTableFunction(self, SourceFun_id):
        sql = f'select * from Table_Function where SourceFun_id={SourceFun_id}'
        return self.__table.selectall(sql)


    def selectFromTableFunctionForNumber(self, id, number):
        sql = 'select * from Table_Function where id=%s limit %s'
        prames = (id, number)
        return self.__table.selectmany(sql, prames)

    def selectAllFromTableFunction(self):
        sql = 'select * from Table_Function'
        return self.__table.selectall(sql)

    def insertDataToTableFunction(self, Function_Content, SourceFun_Id, Mutation_Method, Remark):
        sql = 'INSERT INTO Table_Function(Function_content,SourceFun_id,Mutation_method,Remark) values(%s,%s,%s,%s)'
        prames = (Function_Content, SourceFun_Id, Mutation_Method, Remark)
        return self.__table.insert(sql, prames)

    def insertManyDataToTableFunction(self, lis):
        sql = 'insert into Table_Function(Function_content,SourceFun_id,Mutation_method,Remark) values(%s,%s,%s,%s)'
        return self.__table.insertMany(sql, lis)

    def deleteFromTableFunction(self, id):
        sql = 'delete from Table_Function where id=%s'
        prames = (id,)
        return self.__table.delete(sql, prames)

    def deleteAllFromTableFunction(self):
        sql = 'delete from Table_Function'
        return self.__table.deleteAll(sql)

    def updateDataBaseHandle(self, id, Function_content):
        sql = 'update Table_Function set Function_content= %s where id = %s'
        prames = (Function_content, id)
        return self.__table.update(sql, prames)


class Table_Testcase(object):

    def __init__(self):
        self.__table = DataBaseHandle()

    def selectOneFromTableTestcase(self, id):
        # Note that there is no %d during database operation, all fields are matched with %s.
        sql = 'select * from Table_Testcase where id=%s'
        prames = (id)
        return self.__table.selectOne(sql, prames)

    def selectOneContextFromTestcaseContent(self, id):
        sql = "select Testcase_context from Table_Testcase where id=%s"
        prames = (id)
        return self.__table.selectOne(sql, prames)

    def selectMutationMethodFromTableTestcase(self, Mutation_method):
        sql = f'select * from Table_Testcase where Mutation_method={Mutation_method}'
        return self.__table.selectall(sql)
    
    def selectAllMutationFromTableTestcase(self):
        sql = f'select * from Table_Testcase where Mutation_method>0 and Fuzzing_times=0'
        return self.__table.selectall(sql)

    def selectIdFromTableTestcase(self, id):
        sql = f'select * from Table_Testcase where Id={id}'
        return self.__table.selectall(sql)

    def selectTestcaseIdFromTableJavacResult(self, Returncode):
        sql = f'select Testcase_id from Table_javac_Result where Returncode={Returncode}'
        return self.__table.selectall(sql)

    def selectInterestingTimeFromTableTestcase(self, Interesting_times):
        sql = f'select * from Table_Testcase where Interesting_times={Interesting_times}'
        return self.__table.selectall(sql)

    def selectFuzzingTimeFromTableTestcase(self, Fuzzing_times):
        sql = f'select * from Table_Testcase where Fuzzing_times={Fuzzing_times}; '
        # sql = f'select * from Table_Testcase_m22 where id in (select Testcase_id from Table_javac_Suspicious_Result_m22)'
        return self.__table.selectall(sql)

    def selectInterestingTestcase(self):
        sql = f'select * from Table_Testcase where Fuzzing_times=0 and Mutation_method in (4,5,6,7); '
        return self.__table.selectall(sql)

    def selectNotInterestingTestcase(self):
        sql = f'select * from Table_Testcase where Fuzzing_times=0 and Mutation_method in (1,2,3); '
        return self.__table.selectall(sql)

    # Select number testcases started from id.
    def selectFromTableTestcaseForNumber(self, id, number):
        sql = 'select * from Table_Testcase where id=%s limit %s'
        prames = (id, number)
        return self.__table.selectmany(sql, prames)

    # Select all testcase.
    def selectAllFromTableTestcase(self):
        sql = 'select * from Table_Testcase'
        return self.__table.selectall(sql)

    # Insert a testcase.
    def insertDataToTableTestcase(self, Testcase_context, SourceFun_id, SourceTestcase_id, Fuzzing_times,
                                  Mutation_method, Mutation_times, Interesting_times, Probability, Remark):
        sql = 'INSERT INTO Table_Testcase(Testcase_context, SourceFun_id, SourceTestcase_id, Fuzzing_times,Mutation_method ,Mutation_times,Interesting_times,Probability,Remark) values(%s,%s,%s,%s,%s,%s,%s,%s,%s)'
        prames = (Testcase_context, SourceFun_id, SourceTestcase_id, Fuzzing_times, Mutation_method, Mutation_times,
                  Interesting_times, Probability, Remark)
        return self.__table.insert(sql, prames)

    def insertManyDataToTableTestcase(self, lis):
        sql = 'INSERT INTO Table_Testcase(Testcase_context, SourceFun_id, SourceTestcase_id, Fuzzing_times,Mutation_method ,Mutation_times,Interesting_times,Probability,Remark) values(%s,%s,%s,%s,%s,%s,%s,%s,%s)'
        return self.__table.insertMany(sql, lis)

    # Delete specified testcase.
    def deleteFromTableTestcase(self, id):
        sql = 'delete from Table_Testcase where id=%s'
        prames = (id,)
        return self.__table.delete(sql, prames)

    # Delete all testcases.
    def deleteAllFromTableTestcase(self):
        sql = 'delete from Table_Testcase'
        return self.__table.deleteAll(sql)

    # Updata content of specified testcase.
    def updateDataBaseHandle(self, id, Function_content):
        sql = 'update Table_Testcase set Testcase_context= %s where id = %s'
        prames = (Function_content, id)
        return self.__table.update(sql, prames)

    # Update fuzzing_times and interesting_times of specified testcase.
    def updateFuzzingTimesInterestintTimes(self, Fuzzing_times, Interesting_times, id):
        sql = 'update Table_Testcase set Fuzzing_times= %s ,Interesting_times = %s where id = %s'
        prames = (Fuzzing_times, Interesting_times, id)
        return self.__table.update(sql, prames)
    
    # Update probability of specified testcase.
    def updateProbability(self, Probability, id):
        sql = 'update Table_Testcase set Probability = %s where id = %s'
        prames = (Probability, id)
        return self.__table.update(sql, prames)

    # Update mutation_times of specified testcase.
    def updateMutationTimes(self, MutationTimes, id):
        sql = 'update Table_Testcase set Mutation_times= %s where id = %s'
        prames = (MutationTimes, id)
        return self.__table.update(sql, prames)


class Table_Result(object):

    def __init__(self):
        self.__table = DataBaseHandle()

    # Find id that exists in Table_javac_Result but not in Table_Result.
    def selectFromNotHaveInResult(self):
        sql = 'select distinct Table_javac_Result.Testcase_id from Table_javac_Result left join Table_Result on ' \
              'Table_javac_Result.Testcase_id=Table_Result.Testcase_id where Table_Result.Testcase_id is null and Table_javac_Result.Returncode=0;'
        return self.__table.selectall(sql)

    # Select that not all engines successfully run javac.
    def selectFromNotCompleteInResult(self):
        sql = 'select distinct Testcase_id from Table_javac_Result group by Testcase_id,Returncode having count(Testcase_id)<9;'
        return self.__table.selectall(sql)

    # Select one from Table_Result.
    def selectOneFromTableResult(self, id):
        sql = 'select * from Table_Result where id=%s'
        params = (id)
        return self.__table.selectOne(sql, params)

    # Select all result.
    def selectAllFromTableResult(self):
        sql = 'select * from Table_Result'
        return self.__table.selectall(sql)

    # Select one result.
    def selectByTestcaseIDFromTableResult(self,id):
        sql = 'select * from Table_Result where Testcase_id=%s'
        params = (id)
        return self.__table.selectmany(sql,params)

    # Insert one to Table_javac_Result.
    def insertDataToTablejavacResult(self, Testcase_Id, Testbed_Id, Returncode, Stdout, Stderr, duration_ms, seed_coverage,
                                engine_coverage, Remark):
        sql = 'INSERT INTO Table_javac_Result(Testcase_Id, Testbed_Id, Returncode, Stdout,Stderr ,duration_ms,seed_coverage,engine_coverage,Remark) values(%s,%s,%s,%s,%s,%s,%s,%s,%s)'
        prames = (
            Testcase_Id, Testbed_Id, Returncode, Stdout, Stderr, duration_ms, seed_coverage, engine_coverage, Remark)
        return self.__table.insert(sql, prames)

    def insertDataToTableResult(self, Testcase_Id, Testbed_Id, Returncode, Stdout, Stderr, duration_ms, seed_coverage,
                                engine_coverage, Remark):
        sql = 'INSERT INTO Table_Result(Testcase_Id, Testbed_Id, Returncode, Stdout,Stderr ,duration_ms,seed_coverage,engine_coverage,Remark) values(%s,%s,%s,%s,%s,%s,%s,%s,%s)'
        prames = (
            Testcase_Id, Testbed_Id, Returncode, Stdout, Stderr, duration_ms, seed_coverage, engine_coverage, Remark)
        return self.__table.insert(sql, prames)

    # Insert many to Table_Result.
    def insertManyDataToTableResult(self, lis):
        sql = 'INSERT INTO Table_Result(Testcase_Id, Testbed_Id, Returncode, Stdout,Stderr ,duration_ms,seed_coverage,engine_coverage,Remark) values(%s,%s,%s,%s,%s,%s,%s,%s,%s)'
        return self.__table.insertMany(sql, lis)

    # Delete one.
    def deleteFromTableResult(self, id):
        sql = 'delete from Table_Result where id=%s'
        prames = (id,)
        return self.__table.delete(sql, prames)

    # Delete all.
    def deleteAllFromTableResult(self):
        sql = 'delete from Table_Result'
        return self.__table.deleteAll(sql)

    # Update testcase content for id=id testcase.
    def updateDataBaseHandle(self, id, Function_content):
        sql = 'update Table_Result set Testcase_context= %s where id = %s'
        prames = (Function_content, id)
        return self.__table.update(sql, prames)


class Table_Testbed(object):

    def __init__(self):
        self.__table = DataBaseHandle()

    def selectAllFromTableTestbed(self):
        sql = 'select * from Table_Testbed'
        return self.__table.selectall(sql)

    def selectAllIdAndLocateFromTableTestbed(self):
        sql = 'select Id,Testbed_location from Table_Testbed'
        return self.__table.selectall(sql)


class Table_Suspicious_Result(object):

    def __init__(self):
        self.__table = DataBaseHandle()

    def insertDataToTableSuspiciousResult(self, Error_type, Testcase_id, Function_id, Testbed_id, Remark):
        sql = 'INSERT INTO Table_Suspicious_Result( Error_type, Testcase_id, Function_id, Testbed_id,  Remark) values(%s,%s,%s,%s,%s)'
        prames = (Error_type, Testcase_id, Function_id, Testbed_id, Remark)
        return self.__table.insert(sql, prames)

    def insertDataToTablejavacSuspiciousResult(self, Error_type, Testcase_id, Function_id, Testbed_id, Remark):
        sql = 'INSERT INTO Table_javac_Suspicious_Result( Error_type, Testcase_id, Function_id, Testbed_id,  Remark) values(%s,%s,%s,%s,%s)'
        prames = (Error_type, Testcase_id, Function_id, Testbed_id, Remark)
        return self.__table.insert(sql, prames)

    def selectErrorTypeFromTableFunction(self, ErrorType):
        sql = f"select * from Table_Suspicious_Result where Error_type={ErrorType} ORDER BY Testcase_id"
        return self.__table.selectall(sql)

    def selectIdFromTablejavacSuspiciousResult(self, id):
        sql = f'select * from Table_Suspicious_Result where Function_id={id}'
        return self.__table.selectall(sql)

    def selectIdFromTableFunction(self, id):
        sql = f'select * from Table_Suspicious_Result where Id={id}'
        return self.__table.selectall(sql)
