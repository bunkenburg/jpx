package io.jenetics.jpx.tool;

import static java.lang.String.format;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import io.jenetics.jpx.Track;
import io.jenetics.jpx.TrackSegment;
import io.jenetics.jpx.WayPoint;

final class Tracks {
	private Tracks() {
	}

	public static Collector<WayPoint, ?, Track>
	toTrack(final Duration gap, final int minSegmentSize) {
		return Collectors.collectingAndThen(
			Collectors.toList(),
			points -> toTrack(points, gap, minSegmentSize)
		);
	}

	private static Track toTrack(
		final List<WayPoint> points,
		final Duration gap,
		final int minSegmentSize
	) {
		if (points.size() == 0) {
			return Track.builder().build();
		}
		if (points.size() == 1) {
			final WayPoint point = points.get(0);

			return Track.builder()
					.number(1)
					.cmt(trackCmt(points))
					.addSegment(s -> s.addPoint(point))
				.build();
		}

		final Track.Builder track = Track.builder()
			.number(1)
			.cmt(trackCmt(points));

		ZonedDateTime last = zonedDateTime(points.get(0));
		TrackSegment.Builder segment = TrackSegment.builder();

		for (int i = 1; i < points.size(); ++i) {
			final WayPoint point = points.get(i);
			final ZonedDateTime zdt = zonedDateTime(point);

			if (last.plusNanos(gap.toNanos()).isAfter(zdt)) {
				segment.addPoint(point);
			} else {
				if (segment.points().size() >= minSegmentSize) {
					track.addSegment(segment.build());
				}
				segment = TrackSegment.builder();
			}

			last = zdt;
		}

		return track.build();
	}

	private static ZonedDateTime zonedDateTime(final WayPoint wp) {
		return wp
			.getTime()
			.orElse(ZonedDateTime.of(LocalDateTime.MAX, ZoneId.systemDefault()));
	}

	private static String trackCmt(final List<WayPoint> points) {
		final String start = points.get(0).getTime()
			.map(zdt -> zdt.toOffsetDateTime().toString())
			.orElse("");

		final String end = points.get(points.size() - 1).getTime()
			.map(zdt -> zdt.toOffsetDateTime().toString())
			.orElse("");

		return format("Track[%s to %s]", start, end);
	}

}
