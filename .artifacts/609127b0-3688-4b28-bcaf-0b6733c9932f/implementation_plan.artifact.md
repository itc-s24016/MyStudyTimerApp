# 進捗メーター追加の実施計画

各画面に学習の進捗状況を視覚的に表示するメーターを追加し、ユーザーが直感的に進み具合を把握できるようにします。

## Proposed Changes

### UI コンポーネントの追加

#### [MODIFY] [TaskListScreen.kt](file:///home/s24016/AndroidStudioProjects/MyStudyTimerApp/app/src/main/java/com/example/mystudytimerapp/ui/screens/TaskListScreen.kt)
- `TaskItemRow` 内に、タスクタイトルの下に進捗を示す `LinearProgressIndicator` を追加します。
- **計算ロジック**:
  - 完了済みの場合: 100% (1.0f)
  - 未開始（`remainingSeconds` が null）の場合: 0% (0.0f)
  - 進行中の場合: `(全時間 - 残り時間) / 全時間`
- デザイン: 背景色に馴染む色（`surfaceVariant` など）と強調色（`primary`）を組み合わせ、高さを抑えた控えめなバーにします。

#### [MODIFY] [TimerScreen.kt](file:///home/s24016/AndroidStudioProjects/MyStudyTimerApp/app/src/main/java/com/example/mystudytimerapp/ui/screens/TimerScreen.kt)
- タイマー中央の残り時間表示を囲むように `CircularProgressIndicator` を配置します。
- **計算ロジック**: `(全時間 - 残り時間) / 全時間`
- アニメーション: `animateFloatAsState` を使用し、1秒ごとの減少に合わせてメーターがスムーズに動くようにします。
- デザイン: `Box` を使用して時間テキストと重ね合わせ、ストップウォッチのような外見を実現します。

## Verification Plan

### Automated Tests
- 各状態（未開始、進行中、完了）における進捗率計算の整合性を確認します。

### Manual Verification
1. 学習一覧画面で新しいタスクを追加し、メーターが空であることを確認。
2. タスクを開始して数秒経過後、一覧に戻り、バーが少し進んでいることを確認。
3. タイマー画面で円状のメーターが時間の経過とともに一周に近づいていくことを確認。
4. 学習完了ボタンを押した際、一覧画面でメーターが満タン（100%）になることを確認。
