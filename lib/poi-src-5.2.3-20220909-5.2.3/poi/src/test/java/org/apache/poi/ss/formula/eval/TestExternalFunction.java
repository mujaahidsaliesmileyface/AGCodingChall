/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.ss.formula.eval;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.functions.FreeRefFunction;
import org.apache.poi.ss.formula.udf.AggregatingUDFFinder;
import org.apache.poi.ss.formula.udf.DefaultUDFFinder;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.junit.jupiter.api.Test;

final class TestExternalFunction {

    private static ValueEval myFunc1(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length != 1 || !(args[0] instanceof StringEval)) {
            return ErrorEval.VALUE_INVALID;
        }
        StringEval input = (StringEval) args[0];
        return new StringEval(input.getStringValue() + "abc");
    }

    private static ValueEval myFunc2(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length != 1 || !(args[0] instanceof StringEval)) {
            return ErrorEval.VALUE_INVALID;
        }
        StringEval input = (StringEval) args[0];
        return new StringEval(input.getStringValue() + "abc2");
    }

    /**
     * Checks that an external function can get invoked from the formula evaluator.
     */
    @Test
    void testInvoke() throws IOException {
        try (HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("testNames.xls")) {
            HSSFSheet sheet = wb.getSheetAt(0);

            /*
             * register the two test UDFs in a UDF finder, to be passed to the evaluator
             */
            UDFFinder udff1 = new DefaultUDFFinder(new String[]{"myFunc",},
                                                   new FreeRefFunction[]{TestExternalFunction::myFunc1});
            UDFFinder udff2 = new DefaultUDFFinder(new String[]{"myFunc2",},
                                                   new FreeRefFunction[]{TestExternalFunction::myFunc2,});
            UDFFinder udff = new AggregatingUDFFinder(udff1, udff2);


            HSSFRow row = sheet.getRow(0);
            HSSFCell myFuncCell = row.getCell(1); // =myFunc("_")

            HSSFCell myFunc2Cell = row.getCell(2); // =myFunc2("_")

            HSSFFormulaEvaluator fe = HSSFFormulaEvaluator.create(wb, null, udff);
            assertEquals("_abc", fe.evaluate(myFuncCell).getStringValue());
            assertEquals("_abc2", fe.evaluate(myFunc2Cell).getStringValue());
        }
    }
}
