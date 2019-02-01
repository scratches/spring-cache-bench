package com.example;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import jmh.mbr.junit5.Microbenchmark;

@Measurement(time = 1)
@Warmup(iterations = 1, time = 1)
@Fork(value = 2, warmups = 0)
@BenchmarkMode(Mode.Throughput)
@Microbenchmark
public class CacherBenchmarkIT {

	@Benchmark
	public void bench(MainState state) throws Exception {
		state.run();
	}

	@State(Scope.Thread)
	public static class MainState {

		public static enum Sample {
			manual(ManualApplication.class), raw(RawApplication.class), spring(
					Application.class);
			private Class<?> config;

			private Sample(Class<?> config) {
				this.config = config;
			}
		}

		@Param
		private Sample sample = Sample.spring;

		private ConfigurableApplicationContext context;

		private Timer application;

		public void run() {
			application.time("dummy");
		}

		@Setup
		public void init() {
			context = SpringApplication.run(sample.config);
			application = context.getBean(Timer.class);
			application.time("dummy");
		}

		@TearDown
		public void close() {
			context.close();
		}
	}

	@SpringBootConfiguration
	@ImportAutoConfiguration(CacheAutoConfiguration.class)
	@EnableCaching // (mode = AdviceMode.ASPECTJ)
	static class Application {

		@Bean
		public Timer timer() {
			return new ApplicationTimer();
		}

		static class ApplicationTimer implements Timer {
			@Override
			@Cacheable("time")
			public long time(String dummy) {
				return extract();
			}

			private Long extract() {
				try {
					Thread.sleep(10L);
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new IllegalStateException();
				}
				return System.currentTimeMillis();
			}
		}

		public static void main(String[] args) throws Exception {
			Timer timer = SpringApplication.run(CacherBenchmarkIT.Application.class, args)
					.getBean(Timer.class);
			timer.time("dummy");
			timer.time("dummy");
		}

	}

	@SpringBootConfiguration
	static class RawApplication {

		@Bean
		public Timer timer() {
			return new RawTimer();
		}

		static class RawTimer implements Timer {
			@Override
			public long time(String dummy) {
				return extract();
			}

			private Long extract() {
				try {
					Thread.sleep(10L);
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new IllegalStateException();
				}
				return System.currentTimeMillis();
			}
		}
	}

	@SpringBootConfiguration
	@ImportAutoConfiguration(CacheAutoConfiguration.class)
	@EnableCaching
	static class ManualApplication {

		@Bean
		public Timer timer() {
			return new ManualTimer();
		}

		static class ManualTimer implements Timer {
			@Autowired
			private CacheManager caches;

			@Override
			public long time(String dummy) {
				Cache cache = caches.getCache("time");
				return cache.get(dummy, this::extract);
			}

			private Long extract() {
				try {
					Thread.sleep(10L);
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new IllegalStateException();
				}
				return System.currentTimeMillis();
			}
		}
	}

	interface Timer {
		long time(String dummy);
	}

}
