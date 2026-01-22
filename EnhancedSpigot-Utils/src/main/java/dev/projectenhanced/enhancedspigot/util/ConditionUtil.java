/*
 * Copyright 2026 KPG-TB
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dev.projectenhanced.enhancedspigot.util;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.config.ExpressionConfiguration;

import java.math.MathContext;

public class ConditionUtil {
	public static boolean passCondition(String condition) {
		condition = condition.replaceAll(" ", "");
		boolean check;
		Expression expression = new Expression(
			condition, ExpressionConfiguration.builder()
			.mathContext(MathContext.UNLIMITED)
			.build()
		);
		try {
			check = expression.evaluate()
				.getBooleanValue();
		} catch (Exception e) {
			check = checkStringsEquality(condition);
		}
		return check;
	}

	private static boolean checkStringsEquality(String condition) {
		if (condition.contains("===")) {
			String[] elements = condition.split("===", 2);
			return elements[0].equals(elements[1]);
		}
		if (condition.contains("==")) {
			String[] elements = condition.split("==", 2);
			return elements[0].equalsIgnoreCase(elements[1]);
		}
		return false;
	}
}
