package logger

import (
	"os"

	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
	"gopkg.in/natefinch/lumberjack.v2"
)

var Log *zap.Logger

func Init() {
	// Rolling file for application logs
	fileWriter := zapcore.AddSync(&lumberjack.Logger{
		Filename:   "logs/application.log",
		MaxSize:    10, // megabytes
		MaxBackups: 30,
		MaxAge:     30, // days
	})

	// Rolling file for error logs
	errorFileWriter := zapcore.AddSync(&lumberjack.Logger{
		Filename:   "logs/error.log",
		MaxSize:    10,
		MaxBackups: 30,
		MaxAge:     30,
	})

	encoderConfig := zap.NewProductionEncoderConfig()
	encoderConfig.EncodeTime = zapcore.ISO8601TimeEncoder
	encoder := zapcore.NewJSONEncoder(encoderConfig)

	core := zapcore.NewTee(
		zapcore.NewCore(encoder, zapcore.AddSync(os.Stdout), zap.InfoLevel),
		zapcore.NewCore(encoder, fileWriter, zap.InfoLevel),
		zapcore.NewCore(encoder, errorFileWriter, zap.ErrorLevel),
	)

	Log = zap.New(core, zap.AddCaller())
}
