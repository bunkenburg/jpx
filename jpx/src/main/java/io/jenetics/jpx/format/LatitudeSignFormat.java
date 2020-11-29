/*
 * Java GPX Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmstötter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author:
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmail.com)
 */
package io.jenetics.jpx.format;

import java.text.ParsePosition;
import java.util.Optional;

import io.jenetics.jpx.Latitude;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version 1.4
 * @since 1.4
 */
enum LatitudeSignFormat implements Format<Location> {

	INSTANCE;

	@Override
	public Optional<String> format(final Location value) {
		return value.latitude()
			.map(Latitude::toDegrees)
			.map(v -> Double.compare(v, 0.0) >= 0 ? "+" : "-");
	}

	@Override
	public void parse(CharSequence in, ParsePosition pos, LocationBuilder builder) throws ParseException {
		// parse '-' or '+'
		int i = pos.getIndex();
		char c = in.charAt(i);
		if(c=='-'){
			builder.setLatitudeSign(-1);
			pos.setIndex(i+1);
		}
		else if(c=='+'){
			builder.setLatitudeSign(+1);
			pos.setIndex(i+1);
		}
		else {
			pos.setErrorIndex(i);
			throw new ParseException("bad latitude sign", i);
		}
	}

	@Override
	public String toString() {
		return "+";
	}

}
