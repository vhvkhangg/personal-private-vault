package com.vhvkhangg.personalprivatevault.rag;

/** Second-JVM recovery fixture; contains no Spring runtime, data source or request content. */
public final class RagRetrievalRecoveryControllerFixture {
    private RagRetrievalRecoveryControllerFixture() {}
    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(60_000);
    }
}
