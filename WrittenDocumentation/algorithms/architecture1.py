multi_step_model = tf.keras.models.Sequential()
multi_step_model.add(tf.keras.layers.LSTM(128,
                                input_shape=x_train.shape[-2:],
                                dropout=0.2,
                                return_sequences=True))
multi_step_model.add(tf.keras.layers.LSTM(64,
                                dropout=0.3))
multi_step_model.add(tf.keras.layers.Dense(32))