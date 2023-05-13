import configparser
import os

file_name = os.environ.get('CONFIG_FILE', "config/bny.config")
config: configparser = configparser.RawConfigParser()
config.read(file_name)
