multi_step_model = tf.keras.models.Sequential()
multi_step_model.add(tf.keras.layers.LSTM(128,
                            input_shape=x_train.shape[-2:]))
multi_step_model.add(tf.keras.layers.Dense(256,
                            activation='relu'))
multi_step_model.add(tf.keras.layers.Dropout(0.3))
multi_step_model.add(tf.keras.layers.Dense(128,
                            activation='relu'))
multi_step_model.add(tf.keras.layers.Dropout(0.3))
multi_step_model.add(tf.keras.layers.Dense(64,
                            activation='relu'))