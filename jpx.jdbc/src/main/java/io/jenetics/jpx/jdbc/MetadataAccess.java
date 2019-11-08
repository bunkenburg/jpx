/*
 * Java Genetic Algorithm Library (@__identifier__@).
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
package io.jenetics.jpx.jdbc;

import io.jenetics.facilejdbc.Batch;
import io.jenetics.facilejdbc.Dctor;
import io.jenetics.facilejdbc.Query;
import io.jenetics.jpx.Link;
import io.jenetics.jpx.Metadata;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static io.jenetics.facilejdbc.Dctor.field;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @version !__version__!
 * @since !__version__!
 */
public final class MetadataAccess {
	private MetadataAccess() {}

	private static final Query INSERT_QUERY = Query.of(
		"INSERT INTO metadata(" +
			"name, " +
			"dscr, " +
			"time, " +
			"keywords, " +
			"person_id, " +
			"copyright_id, " +
			"bounds_id" +
		") " +
		"VALUES(" +
			":name, " +
			":dscr, " +
			":time, " +
			":keywords, " +
			":person_id, " +
			":copyright_id, " +
			":bounds_id" +
		")"
	);

	private static final Dctor<Metadata> DCTOR = Dctor.of(
		field("name", Metadata::getName),
		field("dscr", Metadata::getDescription),
		field("time", Metadata::getTime),
		field("keywords", Metadata::getKeywords),
		field(
			"person_id",
			(m, c) -> PersonAccess.insert(m.getAuthor().orElse(null), c)
		),
		field(
			"copyright_id",
			(m, c) -> CopyrightAccess.insert(m.getCopyright().orElse(null), c)
		),
		field(
			"bounds_id",
			(m, c) -> BoundsAccess.insert(m.getBounds().orElse(null), c)
		)
	);

	public static Long insert(final Metadata metadata, final Connection conn)
		throws SQLException
	{
		if (metadata == null || metadata.isEmpty()) return null;

		final Long id = INSERT_QUERY
			.on(metadata, DCTOR)
			.executeInsert(conn)
			.orElseThrow();

		insertLinks(id, metadata.getLinks(), conn);
		return id;
	}

	private static final Query LINK_INSERT_QUERY = Query.of(
		"INSERT INTO metadata_link(metadata_id, link_id " +
		"VALUES(:metadata_id, :link_id);"
	);

	private static void insertLinks(
		final Long id,
		final List<Link> links,
		final Connection conn
	)
		throws SQLException
	{
		final Batch batch = Batch.of(
			links,
			Dctor.of(
				field("metadata_id", r -> id),
				field("link_id", LinkAccess::insert)
			)
		);

		LINK_INSERT_QUERY.executeUpdate(batch, conn);
	}


}