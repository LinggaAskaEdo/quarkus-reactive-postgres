package org.otis.fruit.usecase;

import org.otis.fruit.domain.FruitRepository;
import org.otis.shared.constant.StatusMsgEnum;
import org.otis.shared.dto.DtoResponse;
import org.otis.shared.util.DtoHelper;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CreateFruit {
	private final FruitRepository fruitRepository;

	public CreateFruit(FruitRepository fruitRepository) {
		this.fruitRepository = fruitRepository;
	}

	public Uni<DtoResponse> execute(String name) {
		return fruitRepository.create(name).onItem().ifNotNull()
				.transform(resp -> DtoHelper.constructResponse(StatusMsgEnum.SUCCESS,
						"Data created !!!", resp.toString()))
				.onItem().ifNull()
				.continueWith(() -> DtoHelper.constructResponse(StatusMsgEnum.FAILED,
						"Data not created !!!", null))
				.onFailure().recoverWithItem(failure -> DtoHelper.constructResponse(StatusMsgEnum.FAILED,
						failure.getMessage(), null));
	}
}
