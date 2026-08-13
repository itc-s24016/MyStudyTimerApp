# 学習履歴の削除機能の実装計画

学習履歴画面において、個別の選択削除および全履歴の一括削除機能を追加します。

## Proposed Changes

### データレイヤー (Data Layer)

#### [MODIFY] [StudyTaskDao.kt](file:///home/s24016/AndroidStudioProjects/MyStudyTimerApp/app/src/main/java/com/example/mystudytimerapp/data/StudyTaskDao.kt)
- 複数のタスクを一括削除するための `@Delete` メソッド（引数に `List<StudyTask>` を取るもの）を追加します。

#### [MODIFY] [StudyTaskRepository.kt](file:///home/s24016/AndroidStudioProjects/MyStudyTimerApp/app/src/main/java/com/example/mystudytimerapp/data/StudyTaskRepository.kt)
- DAO の一括削除メソッドを呼び出すメソッドを追加します。

### ViewModelレイヤー (ViewModel Layer)

#### [MODIFY] [TaskListViewModel.kt](file:///home/s24016/AndroidStudioProjects/MyStudyTimerApp/app/src/main/java/com/example/mystudytimerapp/ui/viewmodel/TaskListViewModel.kt)
- `TaskListUiState` に `selectedHistoryIds: Set<Int>` を追加します。
- 以下のメソッドを追加します：
    - `toggleHistorySelection(taskId: Int)`: 履歴の選択状態を反転。
    - `clearHistorySelection()`: 選択状態を解除。
    - `deleteSelectedHistory()`: 選択された履歴を削除。
    - `deleteAllHistory()`: すべての完了済みタスクを削除（既存の `deleteCompletedTasks` を流用または名称変更）。

### UIレイヤー (UI Layer)

#### [MODIFY] [HistoryScreen.kt](file:///home/s24016/AndroidStudioProjects/MyStudyTimerApp/app/src/main/java/com/example/mystudytimerapp/ui/screens/HistoryScreen.kt)
- 各履歴アイテムに `Checkbox` を追加し、タップで選択できるようにします。
- `TopAppBar` に「ゴミ箱」アイコン（選択削除用）と「三点リーダーメニュー」内またはボタンとして「全履歴削除」を追加します。
- 削除実行前に確認ダイアログを表示するようにします。

## Verification Plan

### Automated Tests
- `TaskListViewModel` のユニットテストを追加し、選択削除と全削除が正しくリポジトリを呼び出すことを確認します。

### Manual Verification
1. 学習履歴画面を開く。
2. いくつかの項目にチェックを入れ、削除ボタンを押して、それらが消えることを確認する。
3. 「全履歴削除」ボタンを押し、すべての履歴が消えることを確認する。
4. 削除前に確認ダイアログが表示されることを確認する。
