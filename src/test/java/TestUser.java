public class TestUser {
    public int currHp;

    synchronized public void subtractHp(int val) {
        if (val <= 0) {
            return;
        }
        this.currHp -= val;
    }

    public void attkUser(TestUser targetUser) {
        if (null == targetUser) {
            return;
        }
        synchronized (this) {
            final int dmgPoint = 10;
            targetUser.subtractHp(dmgPoint);
        }
    }
}
