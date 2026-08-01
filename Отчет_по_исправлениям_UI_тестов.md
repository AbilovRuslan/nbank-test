# UI

  ---------------------------------------------------------------------------------------------------------------------------------------------
  Класс              Тест                                        Проблема                       Решение
  ------------------ ------------------------------------------- ------------------------------ -----------------------------------------------
  DepositMoneyUi     userCanDepositMoney                         Отсутствовала проверка, что    Добавлена API-проверка через
                                                                 аккаунт создался перед         `UserSteps.getAllAccounts()` перед депозитом.
                                                                 депозитом.                     

  DepositMoneyUi     userCanDepositValidAmounts                  Отсутствовала проверка, что    Добавлена API-проверка через
                                                                 аккаунт создался перед         `UserSteps.getAllAccounts()` перед депозитом.
                                                                 депозитом.                     

  DepositMoneyUi     userCanMakeMultipleDeposits                 Каждый вызов                   Создан метод `openDepositForExistingAccount()`
                                                                 `prepareAccountForDeposit()`   для повторных депозитов.
                                                                 создавал новый счёт вместо     
                                                                 использования существующего.   

  DepositMoneyUi     shouldRejectEmptyDepositAmount              Не проверялось, что баланс не  Добавлена проверка
                                                                 изменился.                     `assertSingleAccountBalance(0.0)`.

  DepositMoneyUi     userCanDepositNegativeAmountTest            Ошибка отрицательного ввода не Добавлена проверка на отрицательное значение.
                                                                 проверялась.                   

  DepositMoneyUi     shouldRejectDepositExceedingLimit           Изменилось округление лимита.  Тест отключён с комментарием.

  DepositMoneyUi     Все тесты                                   `@UserSession` не создавал     Явное создание пользователя через
                                                                 пользователя.                  `AdminSteps.createUser()` и `authAsUser()`.

  TransferTestUi     userCanTransferMoney                        Не списывались деньги.         Добавлено `checkAlertMessageAndAccept()`.

  TransferTestUi     shouldRejectTransferWithInsufficientFunds   Не проверялся баланс.          Добавлен `assertBalances(0.0, 0.0)`.

  TransferTestUi     shouldRejectTransferWithoutConfirmation     Не проверялся баланс.          Добавлен
                                                                                                `assertBalances(TRANSFER_AMOUNT_LARGE, 0.0)`.

  TransferTestUi     shouldRejectTransferWithEmptyAmount         Не проверялись балансы.        Добавлен `assertBalances(0.0, 0.0)`.

  TransferTestUi     Все тесты                                   Использовались индексы счетов. Введены `senderAccount` и `receiverAccount`.

  UsernameUpdateUi   userCanUpdateName                           Тест мог проходить ложно.      Добавлен
                                                                                                `assertThat(actual).isEqualTo(expected)`.

  UsernameUpdateUi   shouldRejectEmptyOrWhitespaceNames          `isNull()` падал.              Использован `isEqualTo(nameBefore)`.

  UsernameUpdateUi   shouldRejectInvalidFormatNames              Смешаны проверки.              Разделено на два теста.

  UsernameUpdateUi   Все тесты                                   Дублирование подготовки.       Созданы `setupUser()` и `openEditProfile()`.
  ---------------------------------------------------------------------------------------------------------------------------------------------

# API

  ---------------------------------------------------------------------------------------------------------------------------------------
  Класс             Тест                                                Проблема          Решение
  ----------------- --------------------------------------------------- ----------------- -----------------------------------------------
  CreateUserTest    adminCanCreateUserWithCorrectData                   Устаревший        `ValidatedCrudRequester<CreateUserResponse>`.
                                                                        подход.           

  CreateUserTest    adminCanNotCreateUserWithInvalidData                Одна строка       `Arguments` + `List.of(...)`.
                                                                        вместо списка     
                                                                        ошибок.           

  DepositMoney      shouldMaintainCorrectBalanceAfterMultipleDeposits   Проверка в цикле. Вынесена после цикла.

  DepositMoney      getAccountBalance                                   POST вместо       Метод удалён.
                                                                        получения         
                                                                        баланса.          

  DepositMoney      shouldRejectDepositWhenTotalExceedsLimit            Изменилось        `@Disabled`.
                                                                        округление.       

  DepositMoney      shouldRejectInvalidDepositAmounts                   Внутренний класс. `Arguments.of(...)`.

  DepositMoney      shouldThrowWhenDepositingToNonExistentAccount       Нет проверки      `assertThat(errorResponse).contains(...)`.
                                                                        сообщения.        

  TransferTest      Все тесты                                           Зависимость от    Переход на `TransferRequester`.
                                                                        TestAccounts.     

  TransferTest      shouldSuccessfullyTransferMoney                     Нет проверки      Добавлен `assertThat(response)`.
                                                                        успеха.           

  TransferTest      shouldRejectInvalidAmounts                          Зависимость от    Прямая проверка `assertThat(...)`.
                                                                        expectError.      

  TransferTest      shouldRejectTransferExceedingBalance                Случайный баланс. Использованы константы.

  TransferTest      shouldRejectTransferToNonExistingAccount            Нет проверки      Добавлен `ERROR_ACCOUNT_NOT_FOUND`.
                                                                        сообщения.        

  TransferTest      Все тесты                                           Дублирование      Создан `buildRequest(...)`.
                                                                        запроса.          

  UsernameUpdate    shouldUpdateNameWithValidValue                      Hamcrest.         AssertJ.

  UsernameUpdate    shouldRejectInvalidNames                            5 дублирующих     Параметризованный тест.
                                                                        тестов.           

  UsernameUpdate    shouldReturnUnauthorizedWithoutAuth                 Нет проверки 401. Добавлен тест.

  UsernameUpdate    setup()                                             RandomData.       `AdminSteps.createUser()`.

  UsernameUpdate    Все тесты                                           Смешение стилей.  Единый `authSpec`.
  ---------------------------------------------------------------------------------------------------------------------------------------
