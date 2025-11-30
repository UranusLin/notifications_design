package logger

import (
	"os"

	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
	"gopkg.in/natefinch/lumberjack.v2"
)

var (
	Log *zap.Logger
)

func Init() {
	// Application Log
	appWriter := zapcore.AddSync(&lumberjack.Logger{
		Filename:   "logs/application.log",
		MaxSize:    10, // megabytes
		MaxBackups: 30,
		MaxAge:     30, // days
	})

	// Error Log
	errWriter := zapcore.AddSync(&lumberjack.Logger{
		Filename:   "logs/error.log",
		MaxSize:    10, // megabytes
		MaxBackups: 30,
		MaxAge:     30, // days
	})

	// Encoder Config
	encoderConfig := zap.NewProductionEncoderConfig()
	encoderConfig.EncodeTime = zapcore.ISO8601TimeEncoder

	// Core
	core := zapcore.NewTee(
		// Info Level -> application.log
		zapcore.NewCore(
			zapcore.NewJSONEncoder(encoderConfig),
			appWriter,
			zap.LevelEnablerFunc(func(lvl zapcore.Level) bool {
				return lvl >= zapcore.InfoLevel && lvl < zapcore.ErrorLevel
			}),
		),
		// Error Level -> error.log
		zapcore.NewCore(
			zapcore.NewJSONEncoder(encoderConfig),
			errWriter,
			zap.LevelEnablerFunc(func(lvl zapcore.Level) bool {
				return lvl >= zapcore.ErrorLevel
			}),
		),
		// Console Output
		zapcore.NewCore(
			zapcore.NewConsoleEncoder(encoderConfig),
			zapcore.Lock(os.Stdout),
			zapcore.InfoLevel,
		),
	)

	Log = zap.New(core, zap.AddCaller())
}
