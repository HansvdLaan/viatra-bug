package com.vanderhighway.trbac.verifier;

import java.util.HashSet;
import java.util.Set;

import com.vanderhighway.trbac.model.trbac.model.Schedule;
import org.eclipse.viatra.query.runtime.api.IMatchUpdateListener;

public class ListenerFactory {

	
	public static IMatchUpdateListener<Range.Match> getRangeUpdateListener() {
		return new IMatchUpdateListener<Range.Match>() {
			@Override
			public void notifyAppearance(Range.Match match) {
				System.out.printf("[ADD Range Match] %s %n", match.prettyPrint());
			}

			@Override
			public void notifyDisappearance(Range.Match match) {
				System.out.printf("[REM Range Match] %s %n", match.prettyPrint());

			}
		};
	}

	public static IMatchUpdateListener<ScheduleRange.Match> getScheduleRangeUpdateListener() {
		return new IMatchUpdateListener<ScheduleRange.Match>() {
			@Override
			public void notifyAppearance(ScheduleRange.Match match) {
				System.out.printf("[ADD ScheduleRange Match] %s %n", match.prettyPrint());
			}

			@Override
			public void notifyDisappearance(ScheduleRange.Match match) {
				System.out.printf("[REM ScheduleRange Match] %s %n", match.prettyPrint());

			}
		};
	}
	
	public static IMatchUpdateListener<RoleName.Match> getRoleNameUpdateListener() {
		return new IMatchUpdateListener<RoleName.Match>() {
			@Override
			public void notifyAppearance(RoleName.Match match) {
				System.out.printf("[ADD RoleName Match] %s %n", match.prettyPrint());
			}

			@Override
			public void notifyDisappearance(RoleName.Match match) {
				System.out.printf("[REM RoleName Match] %s %n", match.prettyPrint());

			}
		};
	}
}
